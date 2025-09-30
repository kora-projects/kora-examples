package ru.tinkoff.kora.example.http.server;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBodyOutput;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.common.annotation.Json;

/**
 * @see Json - Indicates that response should be serialized as JSON
 * @see HttpMethod#GET - Indicates that GET request is expected
 */
@Component
@HttpController
public final class JsonStreamingGetController {

    private static final Logger logger = LoggerFactory.getLogger(JsonStreamingGetController.class);

    private static final int ELEMENTS = 5;

    @Json
    public record HelloWorldResponse(String greeting) {}

    private final JsonWriter<HelloWorldResponse> jsonWriter;

    public JsonStreamingGetController(JsonWriter<HelloWorldResponse> jsonWriter) {
        this.jsonWriter = jsonWriter;
    }

    @HttpRoute(method = HttpMethod.GET, path = "/json/streaming/flow")
    public HttpServerResponse getFlowPublisher() {
        Flow.Publisher<? extends ByteBuffer> publisher = subscriber -> {
            Flow.Subscription subscribtion = new Flow.Subscription() {

                final AtomicInteger counter = new AtomicInteger();
                final AtomicBoolean closed = new AtomicBoolean(false);

                @Override
                public void request(long n) {
                    if (closed.get()) {
                        return;
                    } else if (n <= 0) {
                        subscriber.onError(new IllegalArgumentException("Rule violated: non-positive requests"));
                        return;
                    }

                    logger.info("Requested {}: {}", counter.get(), n);

                    String res = jsonWriter.toStringUnchecked(new HelloWorldResponse("Hello World " + counter.incrementAndGet()));
                    ByteBuffer buffer = ByteBuffer.wrap((res + "\n").getBytes(StandardCharsets.UTF_8));

                    try {
                        logger.info("Providing {}: {}", counter.get(), n);
                        subscriber.onNext(buffer);
                        logger.info("Provided {}: {}", counter.get(), n);

                        if (counter.get() >= ELEMENTS) {
                            closed.set(true);
                            subscriber.onComplete();
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }

                @Override
                public void cancel() {
                    closed.set(true);
                    logger.info("Canceled");
                }
            };

            subscriber.onSubscribe(subscribtion);
        };

        return HttpServerResponse.of(200, HttpBodyOutput.of("application/stream+json", publisher));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/json/streaming/flux")
    public HttpServerResponse getFlux() {
        var publisher = JdkFlowAdapter.publisherToFlowPublisher(Flux.interval(Duration.ofSeconds(1))
                .take(ELEMENTS)
                .map(n -> new HelloWorldResponse("Hello World " + n))
                .map(r -> ByteBuffer.wrap((jsonWriter.toStringUnchecked(r) + "\n").getBytes(StandardCharsets.UTF_8))));

        return HttpServerResponse.of(200, HttpBodyOutput.of("application/stream+json", publisher));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/json/streaming/os")
    public HttpServerResponse getOS() {
        HttpBodyOutput osBody = HttpBodyOutput.ofBlockingStream(
                "application/stream+json",
                os -> {
                    for (int i = 0; i < ELEMENTS; i++) {
                        String res = jsonWriter.toStringUnchecked(new HelloWorldResponse("Hello World " + i));
                        var buffer = (res + "\n").getBytes(StandardCharsets.UTF_8);
                        os.write(buffer);
                        os.flush();

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        return HttpServerResponse.of(200, osBody);
    }
}
