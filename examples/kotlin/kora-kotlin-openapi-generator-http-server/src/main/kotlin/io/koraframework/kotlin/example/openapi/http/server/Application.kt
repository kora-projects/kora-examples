package io.koraframework.kotlin.example.openapi.http.server

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.Principal
import io.koraframework.common.annotation.Tag
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.common.auth.PrincipalWithScopes
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.http.server.common.auth.HttpServerPrincipalExtractor
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.kotlin.example.openapi.petV3.api.ApiSecurity
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.validation.module.ValidationModule
import io.koraframework.validation.module.http.server.ViolationExceptionHttpServerResponseMapper

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ValidationModule, JsonModule, UndertowPublicHttpServerModule {
    fun customViolationExceptionHttpServerResponseMapper(): ViolationExceptionHttpServerResponseMapper {
        return ViolationExceptionHttpServerResponseMapper { _, exception ->
            HttpServerResponseException.of(400, exception.message)
        }
    }

    // В Kora 2.0 генератор именует теги по порядку security requirement в спецификации,
    // а не по имени схемы: Tag0 = bearerAuth, Tag1 = apiKeyAuth, Tag2 = basicAuth, Tag3 = oAuth
    @Tag(ApiSecurity.SecurityRequirementTag0::class)
    fun bearerHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<String, Principal> {
        return HttpServerPrincipalExtractor { _, _ -> UserPrincipal("name") }
    }

    @Tag(ApiSecurity.SecurityRequirementTag2::class)
    fun basicHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<String, Principal> {
        return HttpServerPrincipalExtractor { _, _ -> UserPrincipal("name") }
    }

    @Tag(ApiSecurity.SecurityRequirementTag1::class)
    fun apiKeyHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<String, Principal> {
        return HttpServerPrincipalExtractor { _, _ -> UserPrincipal("name") }
    }

    @Tag(ApiSecurity.SecurityRequirementTag3::class)
    fun oauthHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<String, PrincipalWithScopes> {
        return HttpServerPrincipalExtractor { _, _ -> UserPrincipal("name") }
    }
}

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
