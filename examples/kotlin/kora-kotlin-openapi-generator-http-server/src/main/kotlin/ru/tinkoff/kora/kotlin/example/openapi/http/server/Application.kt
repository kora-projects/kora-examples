package ru.tinkoff.kora.kotlin.example.openapi.http.server

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Principal
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.http.common.auth.PrincipalWithScopes
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.auth.HttpServerPrincipalExtractor
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.kotlin.example.openapi.petV3.api.ApiSecurity
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.validation.module.ValidationModule
import ru.tinkoff.kora.validation.module.http.server.ViolationExceptionHttpServerResponseMapper
import java.util.concurrent.CompletableFuture

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ValidationModule, JsonModule, UndertowHttpServerModule {
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
