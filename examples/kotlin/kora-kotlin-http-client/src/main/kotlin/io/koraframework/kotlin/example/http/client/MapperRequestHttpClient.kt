package io.koraframework.kotlin.example.http.client

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.common.request.HttpClientRequestMapper
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.body.HttpBodyOutput

@HttpClient("httpClient.default")
interface MapperRequestHttpClient {
    data class UserBody(val id: String)

    @Component
    class UserRequestMapper : HttpClientRequestMapper<UserBody> {
        override fun apply(value: UserBody): HttpBodyOutput = HttpBody.plaintext(value.id)
    }

    @HttpRoute(method = HttpMethod.POST, path = "/mapping_request")
    fun post(@Mapping(UserRequestMapper::class) request: UserBody): HttpResponseEntity<String>
}
