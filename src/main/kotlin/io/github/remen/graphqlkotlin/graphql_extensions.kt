package io.github.remen.graphqlkotlin

import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import kotlinx.coroutines.experimental.future.await
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

suspend fun GraphQL.executeSuspend(
    query: String,
    operationName: String? = null,
    context: Any? = null,
    root: Any? = null,
    variables: Map<String, Any>? = null
): ExecutionResult {
    return executeAsync(ExecutionInput(
        query,
        operationName,
        ContextWithContinuation(coroutineContext(), context),
        root,
        variables)
    ).await()
}

internal data class ContextWithContinuation(val coroutineContext: CoroutineContext, val context: Any?)

// TODO: Replace when https://youtrack.jetbrains.com/issue/KT-17609 is released
private suspend fun coroutineContext(): CoroutineContext =
    suspendCoroutineOrReturn { cont -> cont.context }
