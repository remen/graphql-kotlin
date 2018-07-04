package io.github.remen.graphqlkotlin.execution

import com.fasterxml.jackson.databind.JsonNode
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.DataFetchingEnvironment
import io.github.remen.graphqlkotlin.*
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object FieldWithEnvironmentTest: Spek({
    class FieldWithEnvironmentQuery {
        val hello = "Hello World"

        fun envFirst(env: DataFetchingEnvironment, s: String): String {
            return "${s} ${env.getContext() as String}"
        }
    }

    var graphQL: GraphQL? = null
    var schema: Schema? = null
    beforeGroup {
        graphQL = createGraphQL(FieldWithEnvironmentQuery::class)
        schema = getSchema(graphQL!!)
    }

    describe("the 'envFirst' field") {
        var field: Field? = null

        beforeGroup {
            field = schema!!.queryType.fields!!.find { it.name == "envFirst" }!!
        }

        it("takes exactly one argument called 's'") {
            Assertions.assertThat(field!!.args.map { it.name }).containsExactlyInAnyOrder("s")
        }

        it("concatenates the string with the context") {
            runBlocking {
                val executionResult = createGraphQL(FieldWithEnvironmentQuery::class).executeSuspend(
                    query = "{ envFirst(s: \"Hello\") }",
                    root = FieldWithEnvironmentQuery(),
                    context = "World"
                )
                val json = OBJECT_MAPPER.convertValue(executionResult, JsonNode::class.java)
                Assertions.assertThat(json["errors"]).isNullOrEmpty()
                Assertions.assertThat(json["data"]["envFirst"].asText()).isEqualTo("Hello World")

            }
        }
    }

    describe("the 'echo' field") {
        it("returns whatever is sent to it") {
            runBlocking {
                val executionResult = createGraphQL(FieldWithEnvironmentQuery::class)
                    .executeSuspend(
                        query ="{ echo(s: \"Is there an echo in here?\") }",
                        root = FieldWithEnvironmentQuery()
                    )
                val json = OBJECT_MAPPER.convertValue(executionResult, JsonNode::class.java)
                Assertions.assertThat(json["errors"]).isNullOrEmpty()
                Assertions.assertThat(json["data"]["echo"].asText()).isEqualTo("Is there an echo in here?")
            }
        }
    }
})
