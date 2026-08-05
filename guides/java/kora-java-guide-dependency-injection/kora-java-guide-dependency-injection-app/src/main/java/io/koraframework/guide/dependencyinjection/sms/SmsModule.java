package io.koraframework.guide.dependencyinjection.sms;

import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Module;
import io.koraframework.common.annotation.Tag;
import io.koraframework.guide.dependencyinjection.common.Notifier;

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