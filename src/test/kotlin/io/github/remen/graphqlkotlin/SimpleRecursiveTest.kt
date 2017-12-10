package io.github.remen.graphqlkotlin

import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object SimpleRecursiveTest: Spek({
    class User {
        val bestFriend: User? = null
    }
    class SimpleRecursiveQuery {
        fun user(): User {
            return User()
        }
    }

    var graphQL: GraphQL? = null
    beforeGroup {
        graphQL = createGraphQL(SimpleRecursiveQuery::class)
    }

    describe("the queryType") {
        var queryType: Type? = null

        beforeGroup {
            queryType = getSchema(graphQL!!).queryType
        }

        it("has a single field 'user' with type NON_NULL User") {
            assertThat(queryType!!.fields!!.map { it.name }).containsExactly("user")
            val fieldType = queryType!!.fields!!.find { it.name == "user" }!!.type
            assertThat(fieldType.kind).isEqualTo("NON_NULL")
            val ofType = fieldType.ofType!!
            assertThat(ofType.kind).isEqualTo("OBJECT")
            assertThat(ofType.name).isEqualTo("User")
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

        it("has a single field 'bestFriend' with type (nullable) User") {
            assertThat(userType!!.fields!!.map { it.name }).containsExactly("bestFriend")
            val fieldType = userType!!.fields!!.find { it.name == "bestFriend" }!!.type
            assertThat(fieldType.kind).isEqualTo("OBJECT")
            assertThat(fieldType.name).isEqualTo("User")
        }
    }
})