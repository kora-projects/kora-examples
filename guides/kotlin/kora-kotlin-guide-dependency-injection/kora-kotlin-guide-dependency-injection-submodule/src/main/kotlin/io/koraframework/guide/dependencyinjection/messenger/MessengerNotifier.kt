package io.koraframework.guide.dependencyinjection.messenger

import io.koraframework.application.graph.All
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.guide.dependencyinjection.common.Notifier

@Tag(MessengerModule.MessengerTag::class)
@Component
class MessengerNotifier(
    @Tag(Tag.Any::class) private val messengers: All<Messenger>
) : Notifier {

    override fun notify(user: String, message: String) {
        println("Broadcasting to messengers")
        for (messenger in messengers) {
            messenger.sendMessage("$user@$message")
        }
        println("Messenger broadcast complete")
    }
}
