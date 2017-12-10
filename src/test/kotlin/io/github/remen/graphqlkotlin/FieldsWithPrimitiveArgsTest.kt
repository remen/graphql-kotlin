package io.github.remen.graphqlkotlin

import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object FieldsWithPrimitiveArgsTest : Spek({
    class FieldsWithPrimitiveArgsQuery {
        fun twoArgs(name: String, age: Int): String {
            return "User name $name with age $age"
        }

        fun string(x: String): String {
            return x
        }

        fun integer(x: Int): Int {
            return x
        }

        fun double(x: Double): Double {
            return x
        }

        fun boolean(x: Boolean): Boolean {
            return x
        }

    }

    var graphQL: GraphQL? = null
    beforeGroup {
        graphQL = createGraphQL(FieldsWithPrimitiveArgsQuery::class)
    }

    describe("the twoArgs field") {
        var field: Field? = null

        beforeGroup {
            field = getSchema(graphQL!!).queryType.fields!!.find { it.name == "twoArgs" }!!
        }

        it("takes exactly two arguments called name and age") {
            assertThat(field!!.args.map { it.name }).containsExactlyInAnyOrder("name", "age")
        }
    }

    listOf(
        "string" to "String",
        "integer" to "Int",
        "double" to "Float",
        "boolean" to "Boolean"
    ).forEach { (name, type) ->
        describe("the $name field") {
            var field: Field? = null

            beforeGroup {
                field = getSchema(graphQL!!).queryType.fields!!.find { it.name == name }!!
            }

            it("takes exactly one argument called x with type NON_NULL $type") {
                assertThat(field!!.args.map { it.name }).containsExactly("x")
                val argType = field!!.args.find { it.name == "x" }!!.type
                assertThat(argType.kind).isEqualTo("NON_NULL")
                val ofType = argType.ofType!!
                assertThat(ofType.kind).isEqualTo("SCALAR")
                assertThat(ofType.name).isEqualTo(type)
            }
        }
    }
})