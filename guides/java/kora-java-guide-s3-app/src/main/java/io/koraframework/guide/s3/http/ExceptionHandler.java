package io.koraframework.guide.s3.http;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.guide.s3.dto.ErrorResponse;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.HttpServer;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.json.common.JsonWriter;

@Tag(HttpServer.class)
@Component
public final class ExceptionHandler implements HttpServerInterceptor {

    private final JsonWriter<ErrorResponse> errorJsonWriter;

    public ExceptionHandler(JsonWriter<ErrorResponse> errorJsonWriter) {
        this.errorJsonWriter = errorJsonWriter;
    }

    @Override
    public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) {
        try {
            return chain.process(request);
        } catch (HttpServerResponseException e) {
            return jsonResponse(e.code(), "HTTP_" + e.code(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return jsonResponse(400, "BAD_REQUEST", "Invalid request parameters");
        } catch (SecurityException e) {
            return jsonResponse(403, "FORBIDDEN", "Access denied");
        } catch (Exception e) {
            return jsonResponse(500, "INTERNAL_ERROR", "An unexpected error occurred");
        }
    }

    private HttpServerResponse jsonResponse(int statusCode, String code, String message) {
        return HttpServerResponse.of(statusCode,
                HttpBody.json(this.errorJsonWriter.toByteArray(ErrorResponse.of(code, message))));
    }
}
