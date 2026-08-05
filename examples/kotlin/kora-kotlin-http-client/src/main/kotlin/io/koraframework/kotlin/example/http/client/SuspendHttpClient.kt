package io.koraframework.kotlin.example.http.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.Header
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.annotation.Query

/**
 * Kora 2.0 generates blocking HTTP clients and rejects a `suspend` method outright:
 * "Suspend methods are not supported by the HTTP client generator". A coroutine-facing API
 * therefore belongs to application code — this non-generated default function bridges to the
 * generated blocking call.
 */
@HttpClient("httpClient.default")
interface SuspendHttpClient {

    @HttpRoute(method = HttpMethod.GET, path = "/suspend/{path}")
    fun getBlocking(
        @Path path: String,
        @Query query: String?,
        @Header header: String?
    ): HttpResponseEntity<ByteArray>

    suspend fun get(path: String, query: String?, header: String?): HttpResponseEntity<ByteArray> =
        withContext(Dispatchers.IO) { getBlocking(path, query, header) }
}
