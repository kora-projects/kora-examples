package ru.tinkoff.kora.guide.httpserver.advanced.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.form.FormUrlEncoded
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

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
