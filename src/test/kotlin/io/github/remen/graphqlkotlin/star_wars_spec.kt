package io.github.remen.graphqlkotlin

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.GraphQL
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe

//object SchemaBuilderSpec : Spek({
//    val objectMapper = ObjectMapper()
//    val schema = SchemaBuilder.buildSchema(Droid::class)
//
//    val graphQL = GraphQL.newGraphQL(schema).build()
//
//
//    xdescribe("Star Wars Introspection Tests") {
//        describe("Basic Introspection") {
//            it("Allows querying the schema for types") {
//                val query = """
//                    query IntrospectionTypeQuery {
//                        __schema {
//                            types {
//                                name
//                            }
//                        }
//                    }
//                """
//                val result = objectMapper.convertValue(graphQL.execute(query).getData<Any>(), JsonNode::class.java)
//                val names = result.get("__schema").get("types").map { it.get("name").textValue() }
//
//                assertThat(names).containsExactlyInAnyOrder(
//                    "Droid",
//                    "String",
//                    "Boolean",
//                    "__Schema",
//                    "__Type",
//                    "__TypeKind",
//                    "__Field",
//                    "__InputValue",
//                    "__EnumValue",
//                    "__Directive",
//                    "__DirectiveLocation"
//                )
//            }
//
//            it("Allows querying the schema for object fields") {
//                val query = """
//                query IntrospectionDroidFieldsQuery {
//                  __type(name: "Droid") {
//                    name
//                    fields {
//                      name
//                      type {
//                        name
//                        kind
//                      }
//                    }
//                  }
//                }
//                """
//
//                val expected = """
//                {
//                  "data": {
//                    "__type": {
//                      "name": "Droid",
//                      "fields": [
//                        {
//                          "name": "id",
//                          "type": {
//                            "name": null,
//                            "kind": "NON_NULL"
//                          }
//                        },
//                        {
//                          "name": "name",
//                          "type": {
//                            "name": null,
//                            "kind": "NON_NULL"
//                          }
//                        },
//                        {
//                          "name": "friends",
//                          "type": {
//                            "name": null,
//                            "kind": "LIST"
//                          }
//                        },
//                        {
//                          "name": "appearsIn",
//                          "type": {
//                            "name": null,
//                            "kind": "NON_NULL"
//                          }
//                        },
//                        {
//                          "name": "primaryFunction",
//                          "type": {
//                            "name": "String",
//                            "kind": "SCALAR"
//                          }
//                        }
//                      ]
//                    }
//                  }
//                }
//                """
//                val result = graphQL.execute(query).toSpecification()
//                assertThat(result).isEqualToComparingFieldByFieldRecursively(objectMapper.readValue(expected, Map::class.java))
//            }
//        }
//    }
//
//
//
//    test("it works") {
//        val schema = SchemaBuilder.buildSchema(Droid::class)
//
//        val query = """
//        {
//          __type(name: "Droid") {
//            name
//          }
//        }
//        """
//
//        val expected = """
//        {
//          "data": {
//            "__type": {
//              "name": "Droid"
//            }
//          }
//        }
//        """
//
//        val result = GraphQL.newGraphQL(schema).build().execute(query).toSpecification()
//        assertThat(result).isEqualTo(objectMapper.readValue(expected, Map::class.java))
//    }
//})

class Query {
    fun hero(episode: Episode): Character {
        return Human(
            id = "foobar",
            name = "Luke Skywalker",
            friends = setOf(),
            appearsIn = setOf()
        )
    }
}

data class Type(
    val name: String?,
    val fields: Set<Field>?
)

data class Field(
    val name: String?,
    val type: String?
)
