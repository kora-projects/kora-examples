package io.koraframework.guide.dependencyinjection

interface AuditSink {
    fun record(channel: String, message: String)
}
