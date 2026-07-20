package ru.tinkoff.kora.example.crud.controller;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.HttpServerModule;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.json.common.JsonWriter;
import io.micrometer.core.instrument.config.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.MessageTO;

import java.util.concurrent.TimeoutException;

@Tag(HttpServerModule.class)
@Component
public final class HttpExceptionHandler implements HttpServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpExceptionHandler.class);

    private final JsonWriter<MessageTO> errorJsonWriter;

    public HttpExceptionHandler(JsonWriter<MessageTO> errorJsonWriter) {
        this.errorJsonWriter = errorJsonWriter;
    }

    @Override
    public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) throws Exception {
        try {
            return chain.process(request);
        } catch (Exception e) {
            if (e instanceof HttpServerResponseException ex) {
                return ex;
            }

            var body = HttpBody.json(errorJsonWriter.toByteArray(new MessageTO(e.getMessage())));
            if (e instanceof IllegalArgumentException || e instanceof ValidationException) {
                return HttpServerResponse.of(400, body);
            } else if (e instanceof TimeoutException) {
                return HttpServerResponse.of(408, body);
            } else {
                logger.error("Request '{} {}' failed", request.method(), request.path(), e);
                return HttpServerResponse.of(500, body);
            }
        }
    }
}
