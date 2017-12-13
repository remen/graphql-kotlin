package io.github.remen.graphqlkotlin

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

// Enum classes cannot be nested inside objects in Kotlin
enum class Beverage {
    COFFEE,
    TEA,
    CHOCOLATE
}

object EnumFieldTest : Spek({
    class EnumFieldQuery {
        fun order(beverage: Beverage): Beverage? {
            return beverage
        }
    }


    var schema: Schema? = null
    beforeGroup {
        schema = getSchema(createGraphQL(EnumFieldQuery::class))
    }

    describe("the order field") {
        var field: Field? = null

        beforeGroup {
            field = schema!!.queryType.fields!!.find { it.name == "order" }!!
        }

        it("takes exactly one argument called 'beverage' of type NON_NULL ENUM Beverage") {
            assertThat(field!!.args.map { it.name }).containsExactlyInAnyOrder("beverage")
            val argType = field!!.args.find { it.name == "beverage" }!!.type
            assertThat(argType.kind).isEqualTo("NON_NULL")
            val ofType = argType.ofType!!
            assertThat(ofType.kind).isEqualTo("ENUM")
            assertThat(ofType.name).isEqualTo("Beverage")
        }

        it("has return type (nullable) ENUM Beverage") {
            val fieldType = field!!.type
            assertThat(fieldType.kind).isEqualTo("ENUM")
            assertThat(fieldType.name).isEqualTo("Beverage")
        }
    }

    describe("the Beverage type") {
        var type: Type? = null
        beforeGroup {
            type = schema!!.types.find { it.name == "Beverage" }!!
        }

        it("exists") {
            assertThat(type).isNotNull()
        }

        it("is an enum") {
            assertThat(type!!.kind).isEqualTo("ENUM")
        }

        it("it has the correct enumValues") {
            assertThat(type!!.enumValues!!.map { it.name }).containsExactlyInAnyOrder("COFFEE", "TEA", "CHOCOLATE")
        }
    }
})
