package io.github.remen.graphqlkotlin.outputtypes

import io.github.remen.graphqlkotlin.Type
import io.github.remen.graphqlkotlin.createGraphQL
import io.github.remen.graphqlkotlin.getSchema
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object EmptyClassTest : Spek({
    class EmptyQuery

    val graphQL = createGraphQL(EmptyQuery::class)

    describe("the queryType") {
        var queryType: Type? = null

        beforeGroup {
            queryType = getSchema(graphQL).queryType
        }

        it("has the correct name") {
            assertThat(queryType!!.name).isEqualTo("EmptyQuery")
        }

        it("has no fields") {
            assertThat(queryType!!.fields).isEmpty()
        }
    }
})
