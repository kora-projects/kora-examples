package ru.tinkoff.kora.guide.dependencyinjection.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public final class TempFileStorage<T> implements Storage<T> {

    private final Function<T, byte[]> mapper;

    public TempFileStorage(Function<T, byte[]> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(T data) {
        try {
            Path tempFile = Files.createTempFile("storage-", ".tmp");
            Files.write(tempFile, mapper.apply(data));
            System.out.println("Saved to: " + tempFile.getFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}