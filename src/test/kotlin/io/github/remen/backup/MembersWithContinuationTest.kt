//package io.github.remen.backup
//
//import io.github.remen.graphqlkotlin.membersWithContinuation
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.it
//
//object MembersWithContinuationTest : Spek({
//    class TestClass {
//        suspend fun async(parameter1: Int) {
//
//        }
//
//        fun blocking(parameter2: Int) {
//
//        }
//    }
//
//    it("works") {
//        TestClass::class.membersWithContinuation().map { member ->
//            println("${member.name}: ${member.isSuspend}")
//        }
//
//    }
//})