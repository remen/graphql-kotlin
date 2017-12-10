package io.github.remen.graphqlkotlin

import graphql.Scalars.GraphQLInt
import graphql.Scalars.GraphQLString
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType.newObject
import graphql.schema.GraphQLSchema
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters

fun KClass<*>.membersWithContinuation(): List<KCallableWrapper<*>> {
    val javaMethodsByName = this.java.methods.fold(
        mutableMapOf<String, MutableList<Method>>()
    ) { map, method ->
        map.apply { computeIfAbsent(method.name) { mutableListOf() }.add(method) }
    }

    return this.members.map { callable ->
        val isSuspend = javaMethodsByName[callable.name]!![0].parameterCount != callable.valueParameters.size
        KCallableWrapper(callable, isSuspend)
    }
}

class KCallableWrapper<T>(callable: KCallable<T>, val isSuspend: Boolean) : KCallable<T> by callable

class SchemaBuilder {
    companion object {
        fun buildSchema(any: Any): GraphQLSchema {
            val kClass = any::class

            val builder = newObject().name(kClass.simpleName)

            val fields = kClass.membersWithContinuation().filterNot { isInternal(it) }.map { callable ->
                val valueParameters = callable.valueParameters

                val arguments = valueParameters.map { parameter ->
                    val graphQLType = when (parameter.type) {
                        String::class.starProjectedType -> GraphQLString
                        Int::class.starProjectedType -> GraphQLInt
                        else -> TODO("Unsupported type ${parameter.type}")
                    }

                    if (parameter.type.isMarkedNullable) {
                        GraphQLArgument(parameter.name, graphQLType)
                    } else {
                        GraphQLArgument(parameter.name, GraphQLNonNull(graphQLType))
                    }
                }

                val fieldDefinition = newFieldDefinition()
                    .name(callable.name)
                    .argument(arguments)

                val innerType = when (callable.returnType) {
                    String::class.starProjectedType -> GraphQLString
                    else -> TODO()
                }

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

                fieldDefinition.build()
            }

            builder.fields(fields)

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


fun main(args: Array<String>) {
    class Foobar {
        suspend fun f(x: String): Unit {
            println("Hello $x")
        }
    }

//    val javaMethods = Foobar::class.java.declaredMethods
//    Foobar::class.declaredMembers.forEachIndexed {i, callable ->
//        println(callable.name + " " + javaMethods[i].name)
//
//    }

//
//    val f = Foobar::class.java.methods.find { it.name == "f" }!!
//
//    println(f.typeParameters)
//    println(f.annotations)
//
//    println(f.parameters.map { it.type.name })

}