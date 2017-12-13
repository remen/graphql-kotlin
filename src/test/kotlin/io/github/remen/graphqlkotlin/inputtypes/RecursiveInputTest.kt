package io.github.remen.graphqlkotlin.inputtypes

import io.github.remen.graphqlkotlin.Schema
import io.github.remen.graphqlkotlin.createGraphQL
import io.github.remen.graphqlkotlin.getSchema
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object RecursiveInputTest : Spek({
    data class InceptionQuery(
        val deeper: InceptionQuery?
    )

    var schema: Schema? = null
    beforeGroup {
        val graphQL = createGraphQL(InceptionQuery::class)
        schema = getSchema(graphQL)
    }

    describe("the queryType") {
        it("has the name 'InceptionQuery'") {
            assertThat(schema?.queryType?.name).isEqualTo("InceptionQuery")
        }
        it("has a single field called 'deeper' with type (nullable) InceptionQuery") {

        }
    }
})
