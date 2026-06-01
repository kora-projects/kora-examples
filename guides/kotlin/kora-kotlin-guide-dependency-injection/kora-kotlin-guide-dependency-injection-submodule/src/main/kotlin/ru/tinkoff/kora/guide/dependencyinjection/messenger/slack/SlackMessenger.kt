package ru.tinkoff.kora.guide.dependencyinjection.messenger.slack

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.guide.dependencyinjection.messenger.Messenger

@Tag(SlackMessenger::class)
@Component
class SlackMessenger : Messenger {

    override fun sendMessage(message: String) {
        println("Slack: $message")
    }
}
