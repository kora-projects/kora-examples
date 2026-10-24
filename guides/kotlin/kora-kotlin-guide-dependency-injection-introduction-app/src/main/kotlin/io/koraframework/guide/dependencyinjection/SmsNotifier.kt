package io.koraframework.guide.dependencyinjection

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag

@Tag(SmsTag::class)
@Component
class SmsNotifier(
    private val formatter: MessageFormatter
) : Notifier {

    override fun channel(): String = "sms"

    override fun notifyUser(message: String): String = "SMS: ${formatter.format(message)}"
}
