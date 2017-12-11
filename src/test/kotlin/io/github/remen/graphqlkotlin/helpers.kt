package io.github.remen.graphqlkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import graphql.GraphQL
import kotlin.reflect.KClass

fun createGraphQL(kClass: KClass<*>): GraphQL {
    return GraphQL.newGraphQL(createGraphQLSchema(kClass)).build()
}

fun getSchema(graphQL: GraphQL): Schema {
    val data = graphQL.execute(schemaQuery).getData<Any>()
    val result = objectMapper.convertValue(data, IntroSpectionResult::class.java)
    val __schema = result!!.__schema
    return __schema
}

val objectMapper = ObjectMapper().registerKotlinModule().configure(SerializationFeature.INDENT_OUTPUT, true)

val Any?.json: String
    get() {
        return objectMapper.writeValueAsString(this)
    }

val schemaQuery = """
    {
      __schema {
        types {
          name
          kind
          enumValues {
            name
          }
          fields {
            name
            args {
              name
              type {
                name
                kind
                ofType {
                  name
                  kind
                }
              }
            }
            type {
              name
              kind
              ofType {
                name
                kind
                ofType {
                  name
                  kind
                  ofType {
                    name
                    kind
                  }
                }
              }
            }
          }
        }

        queryType {
          name
          fields {
            name
            args {
              name
              type {
                name
                kind
                ofType {
                  name
                  kind
                }
              }
            }
            type {
              name
              kind
              ofType {
                name
                kind
                ofType {
                  name
                  kind
                  ofType {
                    name
                    kind
                  }
                }
              }
            }
          }
        }
      }
    }
"""

data class FieldType(val name: String?, val kind: String?, val ofType: FieldType?)
data class Arg(val name: String, val type: FieldType)
data class Field(val name: String, val args: List<Arg>, val type: FieldType)
data class EnumValue(val name: String)
data class Type(val name: String, val kind: String?, val fields: List<Field>?, val enumValues: List<EnumValue>?)
data class Schema(val queryType: Type, val types: List<Type>)
data class IntroSpectionResult(val __schema: Schema)