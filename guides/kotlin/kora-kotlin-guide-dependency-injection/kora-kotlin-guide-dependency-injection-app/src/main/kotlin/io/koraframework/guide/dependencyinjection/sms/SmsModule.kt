package io.koraframework.guide.dependencyinjection.sms

import io.koraframework.common.annotation.Module
import io.koraframework.common.annotation.Tag
import io.koraframework.guide.dependencyinjection.common.Notifier

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
