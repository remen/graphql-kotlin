package io.github.remen.graphqlkotlin

import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object FieldWithPrimitiveArgsTest: Spek({
    class FieldWithPrimitiveArgsQuery {
        fun user(name: String, age: Int): String {
            return "User name $name with age $age"
        }
    }

    var graphQL: GraphQL? = null
    beforeGroup {
        graphQL = createGraphQL(FieldWithPrimitiveArgsQuery::class)
    }

    describe("the user field") {
        var userField: Field? = null

        beforeGroup {
            userField = getSchema(graphQL!!).queryType.fields!!.find { it.name == "user" }!!
        }

        it("takes exactly two arguments called name and age") {
            assertThat(userField!!.args.map { it.name }).containsExactlyInAnyOrder("name", "age")
        }
    }
})