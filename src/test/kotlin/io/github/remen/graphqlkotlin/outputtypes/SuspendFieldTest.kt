package io.github.remen.graphqlkotlin.outputtypes

import com.fasterxml.jackson.databind.JsonNode
import graphql.ExecutionInput
import graphql.GraphQL
import io.github.remen.graphqlkotlin.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object SuspendFieldTest : Spek({
    class SuspendableQuery {
        suspend fun suspend(delay: Long): Long {
            delay(delay)
            return delay
        }
    }

    var graphQL: GraphQL? = null
    var queryType: Type? = null
    beforeGroup {
        graphQL = createGraphQL(SuspendableQuery::class)
        queryType = getSchema(graphQL!!).queryType
    }

    describe("the 'suspend' field") {
        var field: Field? = null
        beforeGroup {
            field = queryType!!.fields!!.find { it.name == "suspend" }
        }

        it("exists") {
            assertThat(field).isNotNull()
        }

        it("has type NON_NULL Integer") {
            val type = field!!.type
            assertThat(type.kind).isEqualTo("NON_NULL")
            val ofType = type.ofType!!
            assertThat(ofType.name).isEqualTo("Long")
        }

        it("can be queried against") {
            runBlocking {
                val result = graphQL!!.executeSuspend(
                    query = "{ suspend(delay: 10) }",
                    root = SuspendableQuery()
                )
                val json = OBJECT_MAPPER.convertValue(result, JsonNode::class.java)
                assertThat(json["errors"]).isNullOrEmpty()
                assertThat(json["data"]["suspend"].asLong()).isEqualTo(10)
            }
        }
    }
})
