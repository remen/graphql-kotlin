package io.github.remen.graphqlkotlin

import graphql.Scalars.*
import graphql.schema.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters

class GraphQLSchemaBuilder(val kClass: KClass<*>) {
    private val references = mutableSetOf<String>()

    fun build(): GraphQLSchema {
        return GraphQLSchema.newSchema()
            .query(graphQLObjectType(kClass))
            .build()
    }

    private fun graphQLObjectType(kClass: KClass<*>): GraphQLObjectType {
        try {
            val name = kClass.simpleName!!
            references.add(name)
            return GraphQLObjectType.Builder()
                .name(name)
                .fields(fields(kClass))
                .build()
        } catch (e: Throwable) {
            throw RuntimeException("Error while building type for ${kClass.qualifiedName}", e)
        }
    }

    private fun fields(kClass: KClass<*>): List<GraphQLFieldDefinition> {
        return kClass.members.filterNot {
            val name = it.name
            listOf("name", "copy", "equals", "hashCode", "toString").contains(name) || name.startsWith("component")
        }.mapNotNull { member ->
            val arguments: List<GraphQLArgument> = member.valueParameters.map { kParameter ->
                if (kParameter.type.classifier == Float::class) {
                    throw IllegalArgumentException("Cannot use Float type in input variables (Float type in GraphQL is too big)")
                }

                val argType: GraphQLInputType = graphQLType(kParameter.type) as GraphQLInputType

                GraphQLArgument.newArgument()
                    .name(kParameter.name)
                    .type(argType)
                    .build()
            }

            val returnType: GraphQLOutputType = graphQLType(member.returnType) as GraphQLOutputType
            GraphQLFieldDefinition.newFieldDefinition()
                .name(member.name)
                .type(returnType)
                .argument(arguments)
                .build()
        }
    }

    private fun graphQLCompositeType(kClass: KClass<*>): GraphQLType {
        return when {
            kClass.simpleName!! in references -> GraphQLTypeReference(kClass.simpleName!!)
            kClass.isSubclassOf(Enum::class) -> graphQLEnumType(kClass)
            else -> graphQLObjectType(kClass)
        }
    }

    private fun graphQLType(kType: KType): GraphQLType {
        val kClass = kType.classifier!! as KClass<*>
        val innerType: GraphQLType = when {
            kClass == Int::class -> GraphQLInt
            kClass == Long::class -> GraphQLLong
            kClass == Double::class -> GraphQLFloat
            kClass == Float::class -> GraphQLFloat
            kClass == String::class -> GraphQLString
            kClass == Boolean::class -> GraphQLBoolean
            kClass.isSubclassOf(Collection::class) -> GraphQLList.list(graphQLType(kType.arguments[0].type!!))
            else -> graphQLCompositeType(kClass)
        }

        val type = if (!kType.isMarkedNullable) {
            GraphQLNonNull(innerType)
        } else {
            innerType
        }
        return type
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
