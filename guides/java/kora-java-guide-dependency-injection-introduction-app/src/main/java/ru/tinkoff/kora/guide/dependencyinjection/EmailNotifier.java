package ru.tinkoff.kora.guide.dependencyinjection;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;

@Tag(EmailTag.class)
@Component
public final class EmailNotifier implements Notifier {

    private final MessageFormatter formatter;

    public EmailNotifier(MessageFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public String notifyUser(String message) {
        return "EMAIL: " + formatter.format(message);
    }
}
