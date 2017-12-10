package io.github.remen.backup

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.GraphQL
import io.github.remen.graphqlkotlin.SchemaBuilder
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object HelloWorldTest : Spek({
    class SimpleQuery {
        fun hello(x: String): String {
            return "Hello $x"
        }
    }

    val objectMapper = ObjectMapper()
    val graphQL = GraphQL
        .newGraphQL(SchemaBuilder.buildSchema(SimpleQuery()))
        .build()


    describe("the result of an introspection query") {
        val query = """
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
                    }
                  }
                }
                type {
                  name
                  kind
                  ofType {
                    name
                  }
                }
              }
            }
          }
        }
        """

        it("contains exactly a single field called hello") {
            val result = objectMapper.convertValue(graphQL.execute(query).getData<Any>(), JsonNode::class.java)
            val fieldNames = result.get("__schema").get("queryType").get("fields").map { it.get("name").textValue() }
            Assertions.assertThat(fieldNames).containsExactlyInAnyOrder("hello")
        }

        describe("the hello field") {
            var helloField: JsonNode? = null
            beforeGroup {
                val result = objectMapper.convertValue(graphQL.execute(query).getData<Any>(), JsonNode::class.java)
                helloField = result.get("__schema").get("queryType").get("fields").find { it.get("name").textValue() == "hello" }
            }

            it("takes a single argument 'x' of type non-null string") {
                assertThat(helloField!!.get("args")).hasSize(1)
                val argument = helloField!!.get("args").get(0)
                val argumentType = argument.get("type")

                assertThat(argument.get("name").asText()).isEqualTo("x")
                assertThat(argumentType.get("kind").asText()).isEqualTo("NON_NULL")
                assertThat(argumentType.get("ofType").get("name").asText()).isEqualTo("String")
            }

            it("returns a non-null string") {
                assertThat(helloField!!.get("type").get("kind").textValue()).isEqualTo("NON_NULL")
                assertThat(helloField!!.get("type").get("ofType").get("name").textValue()).isEqualTo("String")
            }
        }
    }

    describe("querying the hello field") {
        val query = """
        {
            hello(x:"World")
        }
        """

        it("works as expected") {
            val result = objectMapper.convertValue(graphQL.execute(query).toSpecification(), JsonNode::class.java)
            assertThat(result.get("errors")).isNullOrEmpty()
            assertThat(result.get("data").get("hello").textValue()).isEqualTo("Hello World")
        }
    }
})

