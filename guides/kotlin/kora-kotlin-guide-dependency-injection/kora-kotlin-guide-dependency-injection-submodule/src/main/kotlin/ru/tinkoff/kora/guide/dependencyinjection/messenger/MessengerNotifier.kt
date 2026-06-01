package ru.tinkoff.kora.guide.dependencyinjection.messenger

import ru.tinkoff.kora.application.graph.All
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.guide.dependencyinjection.common.Notifier

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
