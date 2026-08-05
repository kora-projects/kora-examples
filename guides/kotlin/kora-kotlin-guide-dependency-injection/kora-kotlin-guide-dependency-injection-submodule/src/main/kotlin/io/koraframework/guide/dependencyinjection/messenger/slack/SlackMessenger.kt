package io.koraframework.guide.dependencyinjection.messenger.slack

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.guide.dependencyinjection.messenger.Messenger

@Tag(SlackMessenger::class)
@Component
class SlackMessenger : Messenger {

    override fun sendMessage(message: String) {
        println("Slack: $message")
    }
}
