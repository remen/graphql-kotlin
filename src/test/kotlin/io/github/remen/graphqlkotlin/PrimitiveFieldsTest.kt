package io.github.remen.graphqlkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

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

object PrimitiveFieldsTest : Spek({
    val objectMapper = ObjectMapper().registerKotlinModule()


    class PrimitiveFieldsQuery {
        val integer: Int = 0
        val long: Long = 1
        val double: Double = 2.0
        val float: Float = 3.0F
        val string: String = "4"
        val boolean: Boolean = false

        val nullableInteger: Int? = null
        val nullableLong: Long? = null
        val nullableDouble: Double? = null
        val nullableFloat: Float? = null
        val nullableString: String? = null
        val nullableBoolean: Boolean? = null
    }

    val graphQLSchema = createGraphQLSchema(PrimitiveFieldsQuery::class)
    val graphQL = GraphQL.newGraphQL(graphQLSchema).build()

    describe("the queryType") {
        val data = graphQL.execute(schemaQuery).getData<Any>()
        val result = objectMapper.convertValue(data, IntroSpectionResult::class.java)
        val queryType = result.__schema.queryType

        it("has the correct name") {
            assertThat(queryType.name).isEqualTo("PrimitiveFieldsQuery")
        }

        listOf(
            "integer" to "Int",
            "long" to "Long",
            "double" to "Float", // doubles get converted to Float in output type
            "float" to "Float",
            "string" to "String",
            "boolean" to "Boolean"
        ).forEach { (fieldName, typeName) ->
            describe("the '$fieldName' field") {
                val field = queryType.fields.find { it.name == fieldName }

                it("exists") {
                    assertThat(field).isNotNull()
                }

                it("is of type NON_NULL $typeName") {
                    val type = field!!.type
                    assertThat(type.kind).isEqualTo("NON_NULL")
                    val ofType = type.ofType!!
                    assertThat(ofType.name).isEqualTo(typeName)
                    assertThat(ofType.kind).isEqualTo("SCALAR")
                }
            }
        }

        listOf(
            "nullableInteger" to "Int",
            "nullableLong" to "Long",
            "nullableDouble" to "Float", // doubles get converted to Float in output type
            "nullableFloat" to "Float",
            "nullableString" to "String",
            "nullableBoolean" to "Boolean"
        ).forEach { (fieldName, typeName) ->
            describe("the '$fieldName' field") {
                val field = queryType.fields.find { it.name == fieldName }

                it("exists") {
                    assertThat(field).isNotNull()
                }

                it("is of type (nullable) $typeName") {
                    val type = field!!.type
                    assertThat(type.kind).isEqualTo("SCALAR")
                    assertThat(type.name).isEqualTo(typeName)
                }
            }
        }
    }
})

