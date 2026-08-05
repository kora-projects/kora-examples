package io.koraframework.example.openapi.http.server;

import java.util.concurrent.CompletableFuture;
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.Principal;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.example.openapi.petV3.api.ApiSecurity;
import io.koraframework.http.common.auth.PrincipalWithScopes;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.http.server.common.auth.HttpServerPrincipalExtractor;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.validation.module.ValidationModule;
import io.koraframework.validation.module.http.server.ViolationExceptionHttpServerResponseMapper;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ValidationModule,
        JsonModule,
        UndertowPublicHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    default ViolationExceptionHttpServerResponseMapper customViolationExceptionHttpServerResponseMapper() {
        return (request, exception) -> HttpServerResponseException.of(400, exception.getMessage());
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
