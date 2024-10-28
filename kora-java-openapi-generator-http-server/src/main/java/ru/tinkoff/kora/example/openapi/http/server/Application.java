package ru.tinkoff.kora.example.openapi.http.server;

import java.util.concurrent.CompletableFuture;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Principal;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.example.openapi.petV3.api.ApiSecurity;
import ru.tinkoff.kora.http.common.auth.PrincipalWithScopes;
import ru.tinkoff.kora.http.server.common.auth.HttpServerPrincipalExtractor;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.telemetry.sage.TelemetrySageModule;
import ru.tinkoff.kora.validation.module.ValidationModule;

@KoraApp
public interface Application extends
        TelemetrySageModule,
        HoconConfigModule,
        LogbackModule,
        ValidationModule,
        JsonModule,
        UndertowHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    @Tag(ApiSecurity.BearerAuth.class)
    default HttpServerPrincipalExtractor<Principal> bearerHttpServerPrincipalExtractor() {
        return (request, value) -> CompletableFuture.completedFuture(new UserPrincipal("name"));
    }

    @Tag(ApiSecurity.BasicAuth.class)
    default HttpServerPrincipalExtractor<Principal> basicHttpServerPrincipalExtractor() {
        return (request, value) -> CompletableFuture.completedFuture(new UserPrincipal("name"));
    }

    @Tag(ApiSecurity.ApiKeyAuth.class)
    default HttpServerPrincipalExtractor<Principal> apiKeyHttpServerPrincipalExtractor() {
        return (request, value) -> CompletableFuture.completedFuture(new UserPrincipal("name"));
    }

    @Tag(ApiSecurity.OAuth.class)
    default HttpServerPrincipalExtractor<PrincipalWithScopes> oauthHttpServerPrincipalExtractor() {
        return (request, value) -> CompletableFuture.completedFuture(new UserPrincipal("name"));
    }
}
