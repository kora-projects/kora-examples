package io.koraframework.guide.dependencyinjection

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag

@Tag(EmailTag::class)
@Component
class EmailNotifier(
    private val formatter: MessageFormatter
) : Notifier {

    override fun channel(): String = "email"

    override fun notifyUser(message: String): String = "EMAIL: ${formatter.format(message)}"
}
