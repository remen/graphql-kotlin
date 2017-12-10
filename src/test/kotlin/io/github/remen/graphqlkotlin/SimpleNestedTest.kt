package io.github.remen.graphqlkotlin

import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object SimpleNestedTest : Spek({
    data class User(val id: String = "john.doe")
    class SimpleNestedQuery {
        fun user(): User {
            return User()
        }
    }

    var graphQL: GraphQL? = null
    beforeGroup {
        graphQL = createGraphQL(SimpleNestedQuery::class)
    }

    describe("the queryType") {
        var queryType: Type? = null

        beforeGroup {
            queryType = getSchema(graphQL!!).queryType
        }

        describe("the 'user' field") {
            var field : Field? = null
            beforeGroup {
                field = queryType!!.fields!!.find { it.name == "user" }
            }

            it("exists") {
                assertThat(field).isNotNull()
            }

            it("has type NON_NULL OBJECT with name User") {
                val type = field!!.type
                assertThat(type.kind).isEqualTo("NON_NULL")
                val ofType = type.ofType!!
                assertThat(ofType.kind).isEqualTo("OBJECT")
                assertThat(ofType.name).isEqualTo("User")
            }
        }
    }

    describe("the User type") {
        var userType: Type? = null
        beforeGroup {
            userType = getSchema(graphQL!!).types.find { it.name == "User" }
        }

        it("exists") {
            assertThat(userType).isNotNull()
        }

        it("has a single field 'id' of type NON_NULL String") {
            assertThat(userType!!.fields!!.map { it.name }).containsExactly("id")

            val type = userType!!.fields!!.find {it.name == "id"}!!.type
            assertThat(type.kind).isEqualTo("NON_NULL")
            val ofType = type.ofType!!
            assertThat(ofType.kind).isEqualTo("SCALAR")
            assertThat(ofType.name).isEqualTo("String")
        }
    }
})