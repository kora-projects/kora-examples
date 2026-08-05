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
import java.util.concurrent.CompletableFuture

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ValidationModule, JsonModule, UndertowPublicHttpServerModule {
    fun customViolationExceptionHttpServerResponseMapper(): ViolationExceptionHttpServerResponseMapper {
        return ViolationExceptionHttpServerResponseMapper { _, exception ->
            HttpServerResponseException.of(400, exception.message)
        }
    }

    @Tag(ApiSecurity.BearerAuth::class)
    fun bearerHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<Principal> {
        return HttpServerPrincipalExtractor { _, _ -> CompletableFuture.completedFuture(UserPrincipal("name")) }
    }

    @Tag(ApiSecurity.BasicAuth::class)
    fun basicHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<Principal> {
        return HttpServerPrincipalExtractor { _, _ -> CompletableFuture.completedFuture(UserPrincipal("name")) }
    }

    @Tag(ApiSecurity.ApiKeyAuth::class)
    fun apiKeyHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<Principal> {
        return HttpServerPrincipalExtractor { _, _ -> CompletableFuture.completedFuture(UserPrincipal("name")) }
    }

    @Tag(ApiSecurity.OAuth::class)
    fun oauthHttpServerPrincipalExtractor(): HttpServerPrincipalExtractor<PrincipalWithScopes> {
        return HttpServerPrincipalExtractor { _, _ -> CompletableFuture.completedFuture(UserPrincipal("name")) }
    }
}

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
