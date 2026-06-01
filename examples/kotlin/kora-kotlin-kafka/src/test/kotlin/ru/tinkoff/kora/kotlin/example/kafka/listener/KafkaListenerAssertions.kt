package ru.tinkoff.kora.kotlin.example.kafka.listener

import org.testcontainers.shaded.org.awaitility.Awaitility
import java.time.Duration
import java.util.concurrent.Executors

internal fun awaitReceived(listener: AbstractListener<*>, count: Int = 1) {
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .pollExecutorService(Executors.newSingleThreadExecutor())
        .until { listener.received().size == count }
}

internal fun awaitFailed(listener: AbstractListener<*>, count: Int = 1) {
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .pollExecutorService(Executors.newSingleThreadExecutor())
        .until { listener.failed().size == count }
}
