package ru.tinkoff.kora.guide.openapi.httpserver.advanced;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Principal;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller.DataApiAuthConfig;
import ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller.DataApiPrincipal;
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.ApiSecurity;
import ru.tinkoff.kora.guide.openapi.httpserver.data.model.ErrorResponseTO;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.auth.HttpServerPrincipalExtractor;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.openapi.management.OpenApiManagementModule;
import ru.tinkoff.kora.validation.module.ValidationModule;
import ru.tinkoff.kora.validation.module.http.server.ViolationExceptionHttpServerResponseMapper;

import java.util.concurrent.CompletableFuture;

@KoraApp
public interface Application extends
        HoconConfigModule,
        UndertowHttpServerModule,
        JsonModule,
        LogbackModule,
        ValidationModule,
        OpenApiManagementModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    default ViolationExceptionHttpServerResponseMapper customViolationExceptionHttpServerResponseMapper(
            JsonWriter<ErrorResponseTO> errorResponseJsonWriter) {
        return (request, exception) -> {
            var details = exception.getViolations().stream()
                    .map(v -> "Path " + v.path() + " violated: " + v.message())
                    .toList();

            var response = new ErrorResponseTO("Encountered '%s' validation violations".formatted(details.size()), details);
            return HttpServerResponse.of(
                    400,
                    HttpBody.json(errorResponseJsonWriter.toByteArrayUnchecked(response)));
        };
    }

    @Tag(ApiSecurity.ApiKeyAuth.class)
    default HttpServerPrincipalExtractor<Principal> apiKeyHttpServerPrincipalExtractor(DataApiAuthConfig config) {
        return (request, value) -> {
            if (value == null || !config.value().equals(value)) {
                throw new SecurityException("Invalid API key");
            }
            return CompletableFuture.completedFuture(new DataApiPrincipal("data-api-client"));
        };
    }
}

