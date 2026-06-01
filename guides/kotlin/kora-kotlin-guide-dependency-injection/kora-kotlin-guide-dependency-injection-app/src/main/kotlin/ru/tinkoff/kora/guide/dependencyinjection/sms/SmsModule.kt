package ru.tinkoff.kora.guide.dependencyinjection.sms

import ru.tinkoff.kora.common.Module
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.guide.dependencyinjection.common.Notifier

@Module
interface SmsModule {

    class SmsTag private constructor()

    @Tag(SmsTag::class)
    fun smsNotifier(cellularProvider: SmsCellularProvider?): Notifier {
        return Notifier { user, message ->
            if (cellularProvider == null) {
                println("[SMS] $user@$message")
            } else {
                println("+${cellularProvider.getCode()} [SMS] $user@$message")
            }
        }
    }
}
