package ru.tinkoff.kora.guide.dependencyinjection

import jakarta.annotation.Nullable
import ru.tinkoff.kora.application.graph.All
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root

@Root
@Component
class NotificationService(
    @Tag(Tag.Any::class) private val notifiers: All<Notifier>,
    @Tag(EmailTag::class) private val emailNotifier: Notifier,
    @Nullable private val auditSink: AuditSink?
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
