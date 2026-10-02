package io.koraframework.guide.dependencyinjection;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;

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
