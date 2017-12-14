package io.github.remen.graphqlkotlin

import graphql.Scalars.*
import graphql.schema.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters

class GraphQLSchemaBuilder(private val kClass: KClass<*>) {
    private val references = mutableSetOf<String>()
    private val additionalTypes = mutableListOf<KClass<*>>()

    fun build(): GraphQLSchema {
        return GraphQLSchema.newSchema()
            .query(graphQLObjectType(kClass))
            .additionalTypes(additionalTypes.map { graphQLObjectType(it) }.toSet())
            .build()
    }

    private fun graphQLObjectType(kClass: KClass<*>): GraphQLObjectType {
        val name = kClass.simpleName!!
        references.add(name)

        val interfaces = kClass.supertypes
            .map { (it.classifier as KClass<*>).simpleName }
            .filter { it in references } // TODO: This isn't correct
            .map { GraphQLTypeReference(it) }

        return GraphQLObjectType.Builder()
            .name(name)
            .fields(fields(kClass))
            .apply {
                interfaces.forEach {
                    withInterface(it)
                }
            }
            .build()
    }


    private fun graphQLInputObjectType(kClass: KClass<*>): GraphQLInputObjectType {
        val name = kClass.simpleName!!
        references.add(name)
        return GraphQLInputObjectType
            .newInputObject()
            .name(kClass.simpleName)
            .fields(inputFields(kClass))
            .build()
    }

    private fun inputFields(kClass: KClass<*>): List<GraphQLInputObjectField> {
        return nonInternalMembers(kClass).map { member ->
            GraphQLInputObjectField.newInputObjectField()
                .name(member.name)
                .type(graphQLType(member.returnType, true) as GraphQLInputType)
                .build()
        }
    }

    private fun fields(kClass: KClass<*>): List<GraphQLFieldDefinition> {
        return nonInternalMembers(kClass).map { member ->
            GraphQLFieldDefinition.newFieldDefinition()
                .name(member.name)
                .type(graphQLType(member.returnType, false) as GraphQLOutputType)
                .argument(arguments(member))
                .dataFetcher { env ->
                    val callArgs = member.parameters.associate { kParameter ->
                        kParameter to when (kParameter.kind) {
                            KParameter.Kind.INSTANCE -> env.getSource<Any>()
                            KParameter.Kind.VALUE -> env.getArgument(kParameter.name)
                            KParameter.Kind.EXTENSION_RECEIVER -> throw RuntimeException("Extension methods not supported")
                        }
                    }
                    member.callBy(callArgs)
                }
                .build()
        }
    }

    private fun arguments(member: KCallable<*>): List<GraphQLArgument> {
        val arguments: List<GraphQLArgument> = member.valueParameters.map { kParameter ->
            if (kParameter.type.classifier == Float::class) {
                throw IllegalArgumentException("Cannot use Float type in input variables (Float type in GraphQL is too big)")
            }

            val argType: GraphQLInputType = graphQLType(kParameter.type, true) as GraphQLInputType

            GraphQLArgument.newArgument()
                .name(kParameter.name)
                .type(argType)
                .build()
        }
        return arguments
    }

    private fun nonInternalMembers(kClass: KClass<*>): List<KCallable<*>> {
        return kClass.members.filterNot {
            val name = it.name
            listOf("name", "copy", "equals", "hashCode", "toString").contains(name) || name.startsWith("component")
        }
    }

    private fun graphQLCompositeType(kClass: KClass<*>, isInput: Boolean): GraphQLType {
        return when {
            kClass.simpleName!! in references -> GraphQLTypeReference(kClass.simpleName!!)
            kClass.isSealed -> {
                additionalTypes.addAll(kClass.nestedClasses.filter { it.isFinal && it.isSubclassOf(kClass) })
                graphQLInterfaceType(kClass)
            }
            kClass.isSubclassOf(Enum::class) -> graphQLEnumType(kClass)
            isInput -> graphQLInputObjectType(kClass)
            else -> graphQLObjectType(kClass)
        }
    }

    private fun graphQLInterfaceType(kClass: KClass<*>): GraphQLInterfaceType {
        val name = kClass.simpleName!!
        references.add(name)

        return GraphQLInterfaceType.newInterface()
            .name(kClass.simpleName)
            .fields(fields(kClass))
            .typeResolver { env ->
                // TODO: This isn't very robust, but I'm also not convinced that it should be needed at all
                env.schema.getType(env.getObject<Any>()::class.simpleName) as GraphQLObjectType
            }
            .build()
    }

    private fun graphQLType(kType: KType, isInput: Boolean): GraphQLType {
        val kClass = kType.classifier!! as KClass<*>
        val innerType: GraphQLType = when {
            kClass == Int::class -> GraphQLInt
            kClass == Long::class -> GraphQLLong
            kClass == Double::class -> GraphQLFloat
            kClass == Float::class -> GraphQLFloat
            kClass == String::class -> GraphQLString
            kClass == Boolean::class -> GraphQLBoolean
            kClass.isSubclassOf(Collection::class) -> GraphQLList.list(graphQLType(kType.arguments[0].type!!, isInput))
            else -> graphQLCompositeType(kClass, isInput)
        }

        return if (!kType.isMarkedNullable) {
            GraphQLNonNull(innerType)
        } else {
            innerType
        }
    }

    private fun graphQLEnumType(returnTypeClass: KClass<*>): GraphQLEnumType {
        val name = returnTypeClass.simpleName!!

        references.add(name)

        val values = returnTypeClass.java.enumConstants.map { (it as Enum<*>).name }
        return GraphQLEnumType.newEnum()
            .name(name)
            .apply { values.forEach { value(it) } }
            .build()
    }
}

fun createGraphQLSchema(kClass: KClass<*>): GraphQLSchema {
    return GraphQLSchemaBuilder(kClass).build()
}
