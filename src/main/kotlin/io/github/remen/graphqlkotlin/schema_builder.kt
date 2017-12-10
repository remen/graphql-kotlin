package io.github.remen.graphqlkotlin

import graphql.Scalars.*
import graphql.schema.*
import kotlin.reflect.KClass

fun createGraphQLSchema(kClass: KClass<*>): GraphQLSchema {
    return GraphQLSchema.newSchema()
        .query(GraphQLObjectType.Builder()
            .name(kClass.simpleName)
            .fields(fields(kClass))
        )
        .build()
}

private fun fields(kClass: KClass<*>): List<GraphQLFieldDefinition> {
    return kClass.members.filterNot {
        val name = it.name
        listOf("name", "copy", "equals", "hashCode", "toString").contains(name) || name.startsWith("component")
    }.mapNotNull { member ->
        val innerType = when (member.returnType.classifier) {
            Int::class -> GraphQLInt
            Long::class -> GraphQLLong
            Double::class -> GraphQLFloat
            Float::class -> GraphQLFloat
            String::class -> GraphQLString
            Boolean::class -> GraphQLBoolean
            else -> TODO("No handler for returnType ${member.returnType} on field ${member.name}")
        }

        val type : GraphQLOutputType = if (!member.returnType.isMarkedNullable) {
            GraphQLNonNull(innerType)
        } else {
            innerType
        }

        GraphQLFieldDefinition.newFieldDefinition()
            .name(member.name)
            .type(type)
            .build()
    }
}