package io.github.remen.graphqlkotlin.outputtypes

import io.github.remen.graphqlkotlin.Schema
import io.github.remen.graphqlkotlin.Type
import io.github.remen.graphqlkotlin.createGraphQL
import io.github.remen.graphqlkotlin.getSchema
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

sealed class Character(val id: String) {
    class Mage(id: String, val spells: List<String>): Character(id)
    class Warrior(id: String, val mainWeapon: String): Character(id)
}

object SealedClassTest: Spek({
    class SealedClassQuery {
        fun character(id: String): Character {
            return Character.Warrior("conan", "Broadsword")
        }
    }

    var schema: Schema? = null
    beforeGroup {
        schema = getSchema(createGraphQL(SealedClassQuery::class))
    }

    describe("The Character type") {
        var type : Type? = null
        beforeGroup {
            type = schema!!.types.find { it.name == "Character" }
        }

        it("is an interface") {
            assertThat(type?.kind).isEqualTo("INTERFACE")
        }

        it("has a single field 'id' of type NON_NULL String"){
            assertThat(type?.fields?.map { it.name }).containsExactlyInAnyOrder("id")
            val fieldType = type?.fields?.find { it.name == "id" }?.type
            assertThat(fieldType?.kind).isEqualTo("NON_NULL")
            assertThat(fieldType?.ofType?.name).isEqualTo("String")
        }

        it("has two implementations 'Mage' and 'Warrior'") {
            assertThat(type?.possibleTypes?.map { it.name }).containsExactlyInAnyOrder("Warrior", "Mage")
        }
    }
})
