package io.github.remen.backup

//object SimpleNestedObjectTest: Spek({
//    class User(val id: String)
//
//    class SimpleNestedQuery {
//        fun user(id: String): User {
//            return User(id)
//        }
//    }
//
//    val objectMapper = ObjectMapper()
//    val graphQL = GraphQL
//        .newGraphQL(SchemaBuilder.buildSchema(SimpleNestedQuery()))
//        .build()
//
//
//    describe("the result of an introspection query") {
//        val query = """
//        {
//          __schema {
//            queryType {
//              name
//              fields {
//                name
//                args {
//                  name
//                  type {
//                    name
//                    kind
//                    ofType {
//                      name
//                    }
//                  }
//                }
//                type {
//                  name
//                  kind
//                  ofType {
//                    name
//                  }
//                }
//              }
//            }
//          }
//        }
//        """
//
//        it("contains exactly a single field called user") {
//            val result = objectMapper.convertValue(graphQL.execute(query).getData<Any>(), JsonNode::class.java)
//            val fieldNames = result.get("__schema").get("queryType").get("fields").map { it.get("name").textValue() }
//            Assertions.assertThat(fieldNames).containsExactlyInAnyOrder("user")
//        }
//
//        describe("the user field") {
//            var user: JsonNode? = null
//            beforeGroup {
//                val result = objectMapper.convertValue(graphQL.execute(query).getData<Any>(), JsonNode::class.java)
//                user = result.get("__schema").get("queryType").get("fields").find { it.get("name").textValue() == "user" }
//            }
//
//            it("takes a single argument 'id' of type non-null string") {
//                Assertions.assertThat(user!!.get("args")).hasSize(1)
//                val argument = user!!.get("args").get(0)
//                val argumentType = argument.get("type")
//
//                Assertions.assertThat(argument.get("name").asText()).isEqualTo("id")
//                Assertions.assertThat(argumentType.get("kind").asText()).isEqualTo("NON_NULL")
//                Assertions.assertThat(argumentType.get("ofType").get("name").asText()).isEqualTo("String")
//            }
//
//            it("returns a non-null User") {
//                Assertions.assertThat(user!!.get("type").get("kind").textValue()).isEqualTo("NON_NULL")
//                Assertions.assertThat(user!!.get("type").get("ofType").get("name").textValue()).isEqualTo("User")
//            }
//        }
//    }
//
//    describe("querying the hello field") {
//        val query = """
//        {
//            hello(x:"World")
//        }
//        """
//
//        it("works as expected") {
//            val result = objectMapper.convertValue(graphQL.execute(query).toSpecification(), JsonNode::class.java)
//            Assertions.assertThat(result.get("errors")).isNullOrEmpty()
//            Assertions.assertThat(result.get("data").get("hello").textValue()).isEqualTo("Hello World")
//        }
//    }
//
//
//})