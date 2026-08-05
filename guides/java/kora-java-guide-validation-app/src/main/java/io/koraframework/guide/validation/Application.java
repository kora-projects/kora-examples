package io.koraframework.guide.validation;

import java.util.List;
import java.util.stream.Collectors;
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.guide.validation.dto.ValidationErrorDetails;
import io.koraframework.guide.validation.dto.ValidationErrorResponse;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.HttpServer;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.validation.common.Violation;
import io.koraframework.validation.module.ValidationModule;
import io.koraframework.validation.module.http.server.ValidationHttpServerInterceptor;
import io.koraframework.validation.module.http.server.ViolationExceptionHttpServerResponseMapper;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        ValidationModule,
        UndertowPublicHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    default ViolationExceptionHttpServerResponseMapper violationExceptionHttpServerResponseMapper(
            JsonWriter<ValidationErrorResponse> errorResponseJsonWriter) {
        return (request, exception) -> HttpServerResponse.of(
                400,
                HttpBody.json(errorResponseJsonWriter.toByteArrayUnchecked(
                        ValidationErrorResponse.of(toValidationErrors(exception.getViolations())))));
    }

    @Tag(HttpServer.class)
    default ValidationHttpServerInterceptor validationHttpServerInterceptor(
            ViolationExceptionHttpServerResponseMapper violationExceptionHttpServerResponseMapper) {
        return new ValidationHttpServerInterceptor(violationExceptionHttpServerResponseMapper);
    }

    private static List<ValidationErrorDetails> toValidationErrors(List<Violation> violations) {
        return violations.stream()
                .map(violation -> new ValidationErrorDetails(normalizeField(violation), violation.message()))
                .collect(Collectors.toList());
    }

    private static String normalizeField(Violation violation) {
        String fullPath = violation.path().full();
        int lastDot = fullPath.lastIndexOf('.');
        return lastDot >= 0 ? fullPath.substring(lastDot + 1) : fullPath;
    }
}
