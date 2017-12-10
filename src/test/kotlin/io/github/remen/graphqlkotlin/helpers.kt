package io.github.remen.graphqlkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import graphql.GraphQL
import kotlin.reflect.KClass

fun createGraphQL(kClass: KClass<*>): GraphQL {
    return GraphQL.newGraphQL(createGraphQLSchema(kClass)).build()
}

fun introspectQueryType(graphQL: GraphQL): QueryType {
    val data = graphQL.execute(schemaQuery).getData<Any>()
    val result = objectMapper.convertValue(data, IntroSpectionResult::class.java)
    return result!!.__schema.queryType
}

val objectMapper = ObjectMapper().registerKotlinModule().configure(SerializationFeature.INDENT_OUTPUT, true)

val Any.json: String
    get() {
        return objectMapper.writeValueAsString(this)
    }

val schemaQuery = """
    {
      __schema {
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

data class Type(val name: String?, val kind: String?, val ofType: Type?)
data class Arg(val name: String, val type: Type)
data class Field(val name: String, val args: List<Arg>, val type: Type)
data class QueryType(val name: String, val fields: List<Field>)
data class Schema(val queryType: QueryType)
data class IntroSpectionResult(val __schema: Schema)