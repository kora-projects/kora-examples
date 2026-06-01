package ru.tinkoff.kora.guide.dependencyinjection

interface AuditSink {
    fun record(channel: String, message: String)
}
