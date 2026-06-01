package ru.tinkoff.kora.guide.dependencyinjection.storage;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import ru.tinkoff.kora.common.Module;

@Module
public interface StorageModule {

    default Function<Integer, byte[]> intMapper() {
        return i -> new byte[] {i.byteValue()};
    }

    default Function<String, byte[]> stringMapper() {
        return s -> s.getBytes(StandardCharsets.UTF_8);
    }

    default <T> Storage<T> typedStorage(Function<T, byte[]> mapper) {
        return new TempFileStorage<>(mapper);
    }
}