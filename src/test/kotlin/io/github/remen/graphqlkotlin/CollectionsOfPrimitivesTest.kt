package io.github.remen.graphqlkotlin

import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object CollectionsOfPrimitivesTest : Spek({
    class CollectionsOfPrimitivesQuery {
        val collectionOfInt: Collection<Int> = listOf()
        val listOfInt: List<Int> = listOf()
        val nullableListOfInt: List<Int>? = null
        val listOfNullableInt: List<Int?> = listOf(null)
    }

    var graphQL: GraphQL? = null
    beforeGroup {
        graphQL = createGraphQL(CollectionsOfPrimitivesQuery::class)
    }

    describe("the queryType") {
        var queryType: QueryType? = null

        beforeGroup {
            queryType = introspectQueryType(graphQL!!)
        }

        describe("the 'listOfInt' field") {
            var field : Field? = null
            beforeGroup {
                field = queryType!!.fields.find { it.name == "listOfInt" }
            }

            it("exists") {
                assertThat(field).isNotNull()
            }

            it("has an outer type of NON_NULL List") {
                val type = field!!.type
                assertThat(type.kind).isEqualTo("NON_NULL")
                assertThat(type.ofType!!.kind).isEqualTo("LIST")
            }

            it("has an inner type of NON_NULL Int") {
                val innerType = field!!.type.ofType!!.ofType!!
                assertThat(innerType.kind).isEqualTo("NON_NULL")
                val ofType = innerType.ofType!!
                assertThat(ofType.kind).isEqualTo("SCALAR")
                assertThat(ofType.name).isEqualTo("Int")
            }
        }

        describe("the 'collectionOfInt' field") {
            var field : Field? = null
            beforeGroup {
                field = queryType!!.fields.find { it.name == "collectionOfInt" }
            }

            it("exists") {
                assertThat(field).isNotNull()
            }

            it("has an outer type of NON_NULL List") {
                val type = field!!.type
                assertThat(type.kind).isEqualTo("NON_NULL")
                assertThat(type.ofType!!.kind).isEqualTo("LIST")
            }

            it("has an inner type of NON_NULL Int") {
                val innerType = field!!.type.ofType!!.ofType!!
                assertThat(innerType.kind).isEqualTo("NON_NULL")
                val ofType = innerType.ofType!!
                assertThat(ofType.kind).isEqualTo("SCALAR")
                assertThat(ofType.name).isEqualTo("Int")
            }
        }

        describe("the 'nullableListOfInt' field") {
            var field : Field? = null
            beforeGroup {
                field = queryType!!.fields.find { it.name == "nullableListOfInt" }
            }

            it("exists") {
                assertThat(field).isNotNull()
            }

            it("has an outer type of (nullable) LIST") {
                val type = field!!.type
                assertThat(type.kind).isEqualTo("LIST")
            }

            it("has an inner type of NON_NULL Int") {
                val innerType = field!!.type.ofType!!
                assertThat(innerType.kind).isEqualTo("NON_NULL")
                val ofType = innerType.ofType!!
                assertThat(ofType.kind).isEqualTo("SCALAR")
                assertThat(ofType.name).isEqualTo("Int")
            }
        }
        describe("the 'listOfNullableInt' field") {
            var field : Field? = null
            beforeGroup {
                field = queryType!!.fields.find { it.name == "listOfNullableInt" }
            }

            it("exists") {
                assertThat(field).isNotNull()
            }

            it("has an outer type of NON_NULL LIST") {
                val type = field!!.type
                assertThat(type.kind).isEqualTo("NON_NULL")
                assertThat(type.ofType!!.kind).isEqualTo("LIST")
            }

            it("has an inner type of (nullable) Int") {
                val innerType = field!!.type.ofType!!.ofType!!
                assertThat(innerType.kind).isEqualTo("SCALAR")
                assertThat(innerType.name).isEqualTo("Int")
            }
        }
    }
})