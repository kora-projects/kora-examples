package ru.tinkoff.kora.example.http.server;

import jakarta.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import ru.tinkoff.kora.http.common.body.HttpBodyOutput;

class BlockStreamingHttpBodyOutput implements HttpBodyOutput {

    @FunctionalInterface
    public interface OutputConsumer<T> {

        void accept(T t) throws IOException;
    }

    private final String contentType;
    private final OutputConsumer<OutputStream> outputStreamConsumer;
    private final Closeable closeable;

    BlockStreamingHttpBodyOutput(String contentType,
                                 OutputConsumer<OutputStream> outputStreamConsumer) {
        this(contentType, outputStreamConsumer, () -> {});
    }

    BlockStreamingHttpBodyOutput(String contentType,
                                 OutputConsumer<OutputStream> outputStreamConsumer,
                                 Closeable closeable) {
        this.contentType = contentType;
        this.outputStreamConsumer = outputStreamConsumer;
        this.closeable = closeable;
    }

    @Override
    public long contentLength() {
        return -1;
    }

    @Nullable
    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {}

    @Override
    public void write(OutputStream os) throws IOException {
        outputStreamConsumer.accept(os);
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }
}
