package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

@Component
@HttpController
class JsonPostController {
    @Json
    data class JsonRequest(val id: String)

    @Json
    data class JsonResponse(val name: String, val value: Int)

    @HttpRoute(method = HttpMethod.POST, path = "/json")
    @Json
    fun post(@Json request: JsonRequest): JsonResponse = JsonResponse("Ivan", 100)
}
