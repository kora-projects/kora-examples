package ru.tinkoff.kora.guide.dependencyinjection.storage

import java.nio.charset.StandardCharsets
import java.util.function.Function
import ru.tinkoff.kora.common.Module

@Module
interface StorageModule {

    fun intMapper(): Function<Int, ByteArray> {
        return Function { i -> byteArrayOf(i.toByte()) }
    }

    fun stringMapper(): Function<String, ByteArray> {
        return Function { s -> s.toByteArray(StandardCharsets.UTF_8) }
    }

    fun <T> typedStorage(mapper: Function<T, ByteArray>): Storage<T> {
        return TempFileStorage(mapper)
    }
}
