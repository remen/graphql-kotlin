package io.github.remen.graphqlkotlin.outputtypes

import graphql.GraphQL
import io.github.remen.graphqlkotlin.Type
import io.github.remen.graphqlkotlin.createGraphQL
import io.github.remen.graphqlkotlin.getSchema
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object VisibilityTest : Spek({
    open class QueryWithInvisibleFields {
        val publicField: String = "public"
        private val privateField: String = "private"
        protected val protectedField: String = "protected"
    }

    var graphQL: GraphQL? = null
    beforeGroup {
        graphQL = createGraphQL(QueryWithInvisibleFields::class)
    }

    describe("the queryType") {
        var queryType: Type? = null

        beforeGroup {
            queryType = getSchema(graphQL!!).queryType
        }

        it("only exposes the public field") {
            assertThat(queryType!!.fields!!.map { it.name }).containsExactlyInAnyOrder("publicField")
        }
    }
})
