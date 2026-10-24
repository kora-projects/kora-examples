package io.koraframework.guide.dependencyinjection

import io.koraframework.application.graph.All
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.common.annotation.Root

@Root
@Component
class NotificationService(
    @Tag(Tag.Any::class) private val notifiers: All<Notifier>,
    @Tag(EmailTag::class) private val emailNotifier: Notifier,
    private val auditSink: AuditSink?
) {

    fun broadcast(message: String): List<String> {
        return notifiers.map { notifier ->
            val output = notifier.notifyUser(message)
            auditSink?.record(notifier.channel(), output)
            output
        }
    }

    fun notifyEmailOnly(message: String): String {
        val output = emailNotifier.notifyUser(message)
        auditSink?.record(emailNotifier.channel(), output)
        return output
    }

    fun isAuditEnabled(): Boolean = auditSink != null
}
