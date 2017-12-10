//package io.github.remen.backup
//
//enum class Episode {
//    NEWHOPE, EMPIRE, JEDI
//}
//
//sealed class Character {
//    abstract val id: String
//    abstract val name: String?
//    abstract val friends: Set<Character>
//    abstract val appearsIn: Set<Episode>
//}
//
//data class Human(
//    override val id: String,
//    override val name: String?,
//    override val friends: Set<Character>,
//    override val appearsIn: Set<Episode>
//) : Character()
//
//data class Droid(
//    override val id: String,
//    override val name: String?,
//    override val friends: Set<Character>,
//    override val appearsIn: Set<Episode>,
//    val primaryFunction: String
//) : Character()
