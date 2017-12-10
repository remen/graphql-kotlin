package io.github.remen.graphqlkotlin

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object PrimitiveFieldsTest : Spek({
    class PrimitiveFieldsQuery {
        val integer: Int = 0
        val long: Long = 1
        val double: Double = 2.0
        val float: Float = 3.0F
        val string: String = "4"
        val boolean: Boolean = false

        val nullableInteger: Int? = null
        val nullableLong: Long? = null
        val nullableDouble: Double? = null
        val nullableFloat: Float? = null
        val nullableString: String? = null
        val nullableBoolean: Boolean? = null
    }

    val graphQL = createGraphQL(PrimitiveFieldsQuery::class)

    describe("the queryType") {
        var queryType : QueryType? = null

        beforeGroup {
            queryType = introspectQueryType(graphQL)
        }

        it("has the correct fields (and no more)") {
            assertThat(queryType!!.fields.map { it.name }).containsExactlyInAnyOrder(
                "integer", "long", "double", "float", "string", "boolean",
                "nullableInteger", "nullableLong", "nullableDouble", "nullableFloat", "nullableString", "nullableBoolean"
            )
        }

        listOf(
            "integer" to "Int",
            "long" to "Long",
            "double" to "Float", // doubles get converted to Float in output type
            "float" to "Float",
            "string" to "String",
            "boolean" to "Boolean"
        ).forEach { (fieldName, typeName) ->
            describe("the '$fieldName' field") {
                var field : Field? = null
                beforeGroup {
                    field = queryType!!.fields.find { it.name == fieldName }
                }

                it("exists") {
                    assertThat(field).isNotNull()
                }

                it("is of type NON_NULL $typeName") {
                    val type = field!!.type
                    assertThat(type.kind).isEqualTo("NON_NULL")
                    val ofType = type.ofType!!
                    assertThat(ofType.name).isEqualTo(typeName)
                    assertThat(ofType.kind).isEqualTo("SCALAR")
                }
            }
        }

        listOf(
            "nullableInteger" to "Int",
            "nullableLong" to "Long",
            "nullableDouble" to "Float", // doubles get converted to Float in output type
            "nullableFloat" to "Float",
            "nullableString" to "String",
            "nullableBoolean" to "Boolean"
        ).forEach { (fieldName, typeName) ->
            describe("the '$fieldName' field") {
                var field : Field? = null
                beforeGroup {
                    field = queryType!!.fields.find { it.name == fieldName }
                }
                it("exists") {
                    assertThat(field).isNotNull()
                }

                it("is of type (nullable) $typeName") {
                    val type = field!!.type
                    assertThat(type.kind).isEqualTo("SCALAR")
                    assertThat(type.name).isEqualTo(typeName)
                }
            }
        }
    }
})
