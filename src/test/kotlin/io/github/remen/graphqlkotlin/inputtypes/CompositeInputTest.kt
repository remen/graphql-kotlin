package io.github.remen.graphqlkotlin.inputtypes

import io.github.remen.graphqlkotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object CompositeInputTest : Spek({
    data class Condition(val field: String, val equalTo: String)

    class FieldsWithCompositeArgsQuery {
        fun select(where: Condition): String {
            return "${where.field}='${where.equalTo}'"
        }

        fun selectMany(where: List<Condition>): String {
            return "${where}"
        }

        fun concat(strings: List<String>): String {
            return strings.joinToString("")
        }
    }

    var schema: Schema? = null
    beforeGroup {
        val graphQL = createGraphQL(FieldsWithCompositeArgsQuery::class)
        schema = getSchema(graphQL)
    }

    describe("the select field") {
        var field: Field? = null

        beforeGroup {
            field = schema!!.queryType.fields!!.find { it.name == "select" }!!
        }

        it("takes exactly one argument with type NON_NUlL Condition") {
            assertThat(field!!.args.map { it.name }).containsExactlyInAnyOrder("where")
            val fieldType = field!!.args.find { it.name == "where" }!!.type
            assertThat(fieldType.kind).isEqualTo("NON_NULL")
            assertThat(fieldType.ofType!!.kind).isEqualTo("INPUT_OBJECT")
            assertThat(fieldType.ofType!!.name).isEqualTo("Condition")
        }
    }

    describe("the selectMany field") {
        var field: Field? = null

        beforeGroup {
            field = schema!!.queryType.fields!!.find { it.name == "selectMany" }!!
        }

        it("takes exactly one argument with type NON_NUlL LIST of NON_NULL Condition") {
            assertThat(field!!.args.map { it.name }).containsExactlyInAnyOrder("where")
            val fieldType = field!!.args.find { it.name == "where" }!!.type
            assertThat(fieldType.kind).isEqualTo("NON_NULL")
            assertThat(fieldType.ofType!!.kind).isEqualTo("LIST")
            val innerType = fieldType.ofType.ofType
            assertThat(innerType?.kind).isEqualTo("NON_NULL")
            assertThat(innerType?.ofType?.name).isEqualTo("Condition")
        }
    }

    describe("the concat field") {
        var field: Field? = null

        beforeGroup {
            field = schema!!.queryType.fields!!.find { it.name == "concat" }!!
        }

        it("takes exactly one argument 'strings' with type NON_NULL LIST of NON_NULL String") {
            assertThat(field!!.args.map { it.name }).containsExactlyInAnyOrder("strings")
            val fieldType = field!!.args.find { it.name == "strings" }!!.type
            assertThat(fieldType.kind).isEqualTo("NON_NULL")
            assertThat(fieldType.ofType!!.kind).isEqualTo("LIST")
            val innerType = fieldType.ofType.ofType
            assertThat(innerType?.kind).isEqualTo("NON_NULL")
            assertThat(innerType?.ofType?.name).isEqualTo("String")
        }
    }

    describe("the Condition type") {
        var type : Type? = null
        beforeGroup {
            type = schema?.types?.find { it.name == "Condition" }
        }

        it("exists") {
            assertThat(type).isNotNull()
        }

        it("has exactly two inputFields 'field' and 'equalTo' of type NON_NULL String") {
            assertThat(type?.inputFields?.map { it.name }).containsExactlyInAnyOrder("field", "equalTo")
            type?.inputFields!!.forEach { inputField ->
                assertThat(inputField.type.kind).isEqualTo("NON_NULL")
                assertThat(inputField.type.ofType?.name).isEqualTo("String")
            }
        }
    }
})
