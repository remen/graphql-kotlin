package io.github.remen.graphqlkotlin.execution

import com.fasterxml.jackson.databind.JsonNode
import graphql.ExecutionInput
import io.github.remen.graphqlkotlin.OBJECT_MAPPER
import io.github.remen.graphqlkotlin.createGraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object SimpleExecutionTest : Spek({
    class SimpleExecutionQuery {
        val hello = "Hello World"

        fun echo(s: String): String {
            return s
        }
    }

    describe("the 'hello' field") {
        it("returns 'Hello World'") {
            val executionResult = createGraphQL(SimpleExecutionQuery::class)
                .execute(ExecutionInput("{ hello }", null, null, SimpleExecutionQuery(), mapOf()))
            val json = OBJECT_MAPPER.convertValue(executionResult, JsonNode::class.java)
            assertThat(json["errors"]).isNullOrEmpty()
            assertThat(json["data"]["hello"].asText()).isEqualTo("Hello World")
        }
    }

    describe("the 'echo' field") {
        it("returns whatever is sent to it") {
            val executionResult = createGraphQL(SimpleExecutionQuery::class)
                .execute(ExecutionInput("{ echo(s: \"Is there an echo in here?\") }", null, null, SimpleExecutionQuery(), mapOf()))
            val json = OBJECT_MAPPER.convertValue(executionResult, JsonNode::class.java)
            assertThat(json["errors"]).isNullOrEmpty()
            assertThat(json["data"]["echo"].asText()).isEqualTo("Is there an echo in here?")
        }
    }
})
