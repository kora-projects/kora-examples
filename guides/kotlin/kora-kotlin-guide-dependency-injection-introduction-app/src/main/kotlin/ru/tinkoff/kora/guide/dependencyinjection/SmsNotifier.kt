package ru.tinkoff.kora.guide.dependencyinjection

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag

@Tag(SmsTag::class)
@Component
class SmsNotifier(
    private val formatter: MessageFormatter
) : Notifier {

    override fun channel(): String = "sms"

    override fun notifyUser(message: String): String = "SMS: ${formatter.format(message)}"
}
