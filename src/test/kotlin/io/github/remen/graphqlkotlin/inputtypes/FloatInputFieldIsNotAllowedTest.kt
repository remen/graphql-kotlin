package io.github.remen.graphqlkotlin.inputtypes

import io.github.remen.graphqlkotlin.createGraphQL
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object FloatInputFieldIsNotAllowedTest : Spek({
    class Query {
        fun field(f: Float): Float {
            return f
        }
    }

    describe("The schema builder function") {
        it("throws an exception if a input field is of type Float") {
            val throwable = catchThrowable {
                createGraphQL(Query::class)
            }
            assertThat(throwable)
                .isNotNull()
        }
    }
})
