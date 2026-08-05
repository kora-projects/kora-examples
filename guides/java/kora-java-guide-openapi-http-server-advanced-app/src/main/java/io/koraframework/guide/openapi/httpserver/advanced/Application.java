package io.koraframework.guide.openapi.httpserver.advanced;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.Principal;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.guide.openapi.httpserver.advanced.controller.DataApiAuthConfig;
import io.koraframework.guide.openapi.httpserver.advanced.controller.DataApiPrincipal;
import io.koraframework.guide.openapi.httpserver.data.api.ApiSecurity;
import io.koraframework.guide.openapi.httpserver.data.model.ErrorResponseTO;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.auth.HttpServerPrincipalExtractor;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.openapi.management.OpenApiManagementModule;
import io.koraframework.validation.module.ValidationModule;
import io.koraframework.validation.module.http.server.ViolationExceptionHttpServerResponseMapper;

import java.util.concurrent.CompletableFuture;

@KoraApp
public interface Application extends
        HoconConfigModule,
        UndertowPublicHttpServerModule,
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
                    HttpBody.json(errorResponseJsonWriter.toByteArray(response)));
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

