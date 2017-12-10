package io.github.remen.graphqlkotlin

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.GraphQL
import kotlinx.coroutines.experimental.delay
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object SuspendableFunctionTest : Spek({
    class SuspendableQuery {
        suspend fun suspend(waitMs: Int): String {
            delay(waitMs.toLong())
            return "Waited for $waitMs ms"
        }
    }

    val objectMapper = ObjectMapper()
    val graphQL = GraphQL.newGraphQL(SchemaBuilder.buildSchema(SuspendableQuery())).build()

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

        it("contains exactly a single field called suspend") {
            val result = objectMapper.convertValue(graphQL.execute(query).getData<Any>(), JsonNode::class.java)
            val fieldNames = result.get("__schema").get("queryType").get("fields").map { it.get("name").textValue() }
            Assertions.assertThat(fieldNames).containsExactlyInAnyOrder("suspend")
        }

        describe("the suspend field") {
            var suspendField: JsonNode? = null
            beforeGroup {
                val result = objectMapper.convertValue(graphQL.execute(query).getData<Any>(), JsonNode::class.java)
                suspendField = result.get("__schema").get("queryType").get("fields").find { it.get("name").textValue() == "suspend" }
            }

            it("takes a single argument 'waitMs' of type non-null Int") {
                Assertions.assertThat(suspendField!!.get("args")).hasSize(1)
                val argument = suspendField!!.get("args").get(0)
                val argumentType = argument.get("type")

                Assertions.assertThat(argument.get("name").asText()).isEqualTo("waitMs")
                Assertions.assertThat(argumentType.get("kind").asText()).isEqualTo("NON_NULL")
                Assertions.assertThat(argumentType.get("ofType").get("name").asText()).isEqualTo("Int")
            }

            it("returns a non-null string") {
                Assertions.assertThat(suspendField!!.get("type").get("kind").textValue()).isEqualTo("NON_NULL")
                Assertions.assertThat(suspendField!!.get("type").get("ofType").get("name").textValue()).isEqualTo("String")
            }
        }
    }

    describe("querying the suspend field") {
        val query = """
        {
            suspend(waitMs: 100)
        }
        """

        it("works as expected") {
            val result = objectMapper.convertValue(graphQL.execute(query).toSpecification(), JsonNode::class.java)
            assertThat(result.get("errors")).isNullOrEmpty()
            Assertions.assertThat(result.get("data").get("suspend").textValue()).isEqualTo("Waited for 100 ms")
        }
    }
})