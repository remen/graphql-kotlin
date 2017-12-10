package io.github.remen.graphqlkotlin

val schemaQuery = """
    {
      __schema {
        queryType {
          name
          fields {
            name
            args {
              name
              type {
                name
                kind
                ofType {
                  name
                  kind
                }
              }
            }
            type {
              name
              kind
              ofType {
                name
                kind
              }
            }
          }
        }
      }
    }
"""

data class Type(val name: String?, val kind: String?, val ofType: Type?)
data class Arg(val name: String, val type: Type)
data class Field(val name: String, val args: List<Arg>, val type: Type)
data class QueryType(val name: String, val fields: List<Field>)
data class Schema(val queryType: QueryType)
data class IntroSpectionResult(val __schema: Schema)