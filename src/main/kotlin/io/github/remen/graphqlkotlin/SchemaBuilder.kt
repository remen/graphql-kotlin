package io.github.remen.graphqlkotlin

import graphql.Scalars.GraphQLString
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType.newObject
import graphql.schema.GraphQLSchema
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters


class SchemaBuilder {
    companion object {
        fun buildSchema(any: Any): GraphQLSchema {
            val kClass = any::class

            val builder = newObject().name(kClass.simpleName)

            kClass.members.filterNot { isInternal(it) }.forEach { callable ->
                val valueParameters = callable.valueParameters

                val arguments = valueParameters.map { parameter ->
                    if (parameter.type.isMarkedNullable) {
                        GraphQLArgument(parameter.name, GraphQLString)
                    } else {
                        GraphQLArgument(parameter.name, GraphQLNonNull(GraphQLString))
                    }
                }

                val fieldDefinition = newFieldDefinition()
                    .name(callable.name)
                    .argument(arguments)

                if (callable.returnType.isMarkedNullable) {
                    fieldDefinition.type(GraphQLString)
                } else {
                    fieldDefinition.type(GraphQLNonNull(GraphQLString))
                }

                fieldDefinition
                    .dataFetcher { environment ->
                        val callArgs = mutableMapOf<KParameter, Any>()
                        callArgs[callable.instanceParameter!!] = any

                        environment.arguments.map { (k, v) ->
                            callArgs[callable.findParameterByName(k)!!] = v
                        }

                        callable.callBy(callArgs)
                    }

                builder.field(
                    fieldDefinition
                )
            }

            return GraphQLSchema(builder.build())
        }

        /**
         * Not the best implementation, but will have to do for now
         */
        fun isInternal(callable: KCallable<*>): Boolean {
            val name = callable.name
            return listOf("name", "copy", "equals", "hashCode", "toString").contains(name) || name.startsWith("component")
        }
    }
}
