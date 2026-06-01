package ru.tinkoff.kora.guide.dependencyinjection.storage

import java.nio.file.Files
import java.util.function.Function

class TempFileStorage<T>(
    private val mapper: Function<T, ByteArray>
) : Storage<T> {

    override fun save(data: T) {
        val tempFile = Files.createTempFile("storage-", ".tmp")
        Files.write(tempFile, mapper.apply(data))
        println("Saved to: ${tempFile.fileName}")
    }
}
