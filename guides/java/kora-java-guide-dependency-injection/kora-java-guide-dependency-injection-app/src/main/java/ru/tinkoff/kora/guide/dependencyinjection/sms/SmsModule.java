package ru.tinkoff.kora.guide.dependencyinjection.sms;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.common.Module;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.guide.dependencyinjection.common.Notifier;

@Module
public interface SmsModule {

    final class SmsTag {
        private SmsTag() {}
    }

    @Tag(SmsTag.class)
    default Notifier smsNotifier(@Nullable SmsCellularProvider cellularProvider) {
        return (user, message) -> {
            if (cellularProvider == null) {
                System.out.println("[SMS] " + user + "@" + message);
            } else {
                System.out.println("+" + cellularProvider.getCode() + " [SMS] " + user + "@" + message);
            }
        };
    }
}