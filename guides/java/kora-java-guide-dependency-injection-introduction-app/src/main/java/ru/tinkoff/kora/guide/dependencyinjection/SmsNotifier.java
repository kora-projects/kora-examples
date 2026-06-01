package ru.tinkoff.kora.guide.dependencyinjection;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;

@Tag(SmsTag.class)
@Component
public final class SmsNotifier implements Notifier {

    private final MessageFormatter formatter;

    public SmsNotifier(MessageFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public String channel() {
        return "sms";
    }

    @Override
    public String notifyUser(String message) {
        return "SMS: " + formatter.format(message);
    }
}
