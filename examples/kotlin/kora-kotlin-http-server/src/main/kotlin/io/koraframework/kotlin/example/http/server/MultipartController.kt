package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.response.HttpServerResponse

@Component
@HttpController
class MultipartController {
    @HttpRoute(method = HttpMethod.POST, path = "/multipart")
    fun post(multipart: FormMultipart): HttpServerResponse {
        val partAsString = multipart.parts().map { it.name() }.sorted().joinToString(",")
        return HttpServerResponse.of(200, HttpBody.plaintext(partAsString))
    }
}
