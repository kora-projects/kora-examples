package io.koraframework.guide.openapi.httpserver.advanced.controller;

import io.koraframework.common.annotation.Component;
import io.koraframework.guide.openapi.httpserver.data.model.ErrorResponseTO;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.validation.common.ViolationException;

@Component
public final class DataApiExceptionHandler implements HttpServerInterceptor {

    private final JsonWriter<ErrorResponseTO> errorJsonWriter;

    public DataApiExceptionHandler(JsonWriter<ErrorResponseTO> errorJsonWriter) {
        this.errorJsonWriter = errorJsonWriter;
    }

    @Override
    public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) throws Exception {
        try {
            return chain.process(request);
        } catch (ViolationException e) {
            // left to ViolationExceptionHttpServerResponseMapper, which renders the violations
            throw e;
        } catch (HttpServerResponseException e) {
            return jsonResponse(e.code(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return jsonResponse(400, "Invalid request parameters");
        } catch (SecurityException e) {
            return jsonResponse(403, e.getMessage() != null ? e.getMessage() : "Access denied");
        } catch (Exception e) {
            return jsonResponse(500, "An unexpected error occurred");
        }
    }

    private HttpServerResponse jsonResponse(int statusCode, String message) {
        return HttpServerResponse.of(statusCode, HttpBody.json(this.errorJsonWriter.toByteArray(new ErrorResponseTO(message))));
    }
}
