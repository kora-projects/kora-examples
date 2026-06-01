package ru.tinkoff.kora.guide.validation;

import java.util.List;
import java.util.stream.Collectors;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.guide.validation.dto.ValidationErrorDetails;
import ru.tinkoff.kora.guide.validation.dto.ValidationErrorResponse;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerModule;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.validation.common.Violation;
import ru.tinkoff.kora.validation.module.ValidationModule;
import ru.tinkoff.kora.validation.module.http.server.ValidationHttpServerInterceptor;
import ru.tinkoff.kora.validation.module.http.server.ViolationExceptionHttpServerResponseMapper;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        ValidationModule,
        UndertowHttpServerModule {

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

    @Tag(HttpServerModule.class)
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
