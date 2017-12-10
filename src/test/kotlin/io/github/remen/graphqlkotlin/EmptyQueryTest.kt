package io.github.remen.graphqlkotlin

import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object EmptyQueryTest : Spek({
    class EmptyQuery

    val graphQL = GraphQL.newGraphQL(createGraphQLSchema(EmptyQuery::class)).build()

    describe("the queryType") {
        var queryType: QueryType? = null

        beforeGroup {
            queryType = introspectQueryType(graphQL)
        }

        it("has the correct name") {
            assertThat(queryType!!.name).isEqualTo("EmptyQuery")
        }

        it("has no fields") {
            assertThat(queryType!!.fields).isEmpty()
        }
    }
})