package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.form.FormUrlEncoded
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

@Component
@HttpController
class DataController {

    @HttpRoute(method = HttpMethod.POST, path = "/data/form")
    fun processForm(formBody: FormUrlEncoded): String {
        val name = formBody.get("name")?.values()?.firstOrNull() ?: "World"
        if (name.equals("admin", ignoreCase = true)) {
            throw RestrictedFormNameException(name)
        }
        return "Hello World, $name"
    }

    @HttpRoute(method = HttpMethod.POST, path = "/data/upload")
    @Json
    fun processUpload(multipart: FormMultipart): UploadResponse {
        val fileNames = multipart.parts().map { it.name() }.sorted()
        return UploadResponse(fileNames.size, fileNames)
    }

    @HttpRoute(method = HttpMethod.POST, path = "/data/mapping-request")
    fun processMappedRequest(body: String): String {
        return "Received mapped body: $body"
    }

    @HttpRoute(method = HttpMethod.GET, path = "/data/mapping-by-code/{code}")
    @Json
    fun mappingByCode(@Path code: Int): Payload {
        if (code == 200) {
            return Payload("Hello from response mapper")
        }
        throw HttpServerResponseException.of(code, "Request failed with code $code")
    }
}

@Json
data class Payload(val message: String)

@Json
data class UploadResponse(val fileCount: Int, val fileNames: List<String>)
