package io.github.remen.graphqlkotlin

import graphql.Scalars.*
import graphql.schema.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

class GraphQLSchemaBuilder(val kClass: KClass<*>) {
    private val references = mutableSetOf<String>()

    fun build(): GraphQLSchema {
        return GraphQLSchema.newSchema()
            .query(graphQLObjectType(kClass))
            .build()
    }

    private fun graphQLObjectType(kClass: KClass<*>): GraphQLObjectType {
        val name = kClass.simpleName!!
        references.add(name)
        return GraphQLObjectType.Builder()
            .name(name)
            .fields(fields(kClass))
            .build()
    }

    private fun fields(kClass: KClass<*>): List<GraphQLFieldDefinition> {
        return kClass.members.filterNot {
            val name = it.name
            listOf("name", "copy", "equals", "hashCode", "toString").contains(name) || name.startsWith("component")
        }.mapNotNull { member ->
            val type: GraphQLOutputType = graphQLOutputType(member.returnType)

            GraphQLFieldDefinition.newFieldDefinition()
                .name(member.name)
                .type(type)
                .build()
        }
    }

    private fun graphQLOutputType(kType: KType): GraphQLOutputType {
        val returnTypeClass = kType.classifier!! as KClass<*>
        val innerType: GraphQLOutputType = when {
            returnTypeClass == Int::class -> GraphQLInt
            returnTypeClass == Long::class -> GraphQLLong
            returnTypeClass == Double::class -> GraphQLFloat
            returnTypeClass == Float::class -> GraphQLFloat
            returnTypeClass == String::class -> GraphQLString
            returnTypeClass == Boolean::class -> GraphQLBoolean
            returnTypeClass.isSubclassOf(Collection::class) -> GraphQLList.list(graphQLOutputType(kType.arguments[0].type!!))
            else -> {
                if (returnTypeClass.simpleName in references) {
                    GraphQLTypeReference(returnTypeClass.simpleName)
                } else {
                    graphQLObjectType(returnTypeClass)
                }
            }
        }

        val type: GraphQLOutputType = if (!kType.isMarkedNullable) {
            GraphQLNonNull(innerType)
        } else {
            innerType
        }
        return type
    }
}

fun createGraphQLSchema(kClass: KClass<*>): GraphQLSchema {
    return GraphQLSchemaBuilder(kClass).build()
}
