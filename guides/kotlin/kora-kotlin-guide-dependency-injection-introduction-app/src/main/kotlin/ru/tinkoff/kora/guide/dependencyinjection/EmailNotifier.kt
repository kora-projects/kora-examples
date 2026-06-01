package ru.tinkoff.kora.guide.dependencyinjection

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag

@Tag(EmailTag::class)
@Component
class EmailNotifier(
    private val formatter: MessageFormatter
) : Notifier {

    override fun channel(): String = "email"

    override fun notifyUser(message: String): String = "EMAIL: ${formatter.format(message)}"
}
