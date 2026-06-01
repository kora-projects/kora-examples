package ru.tinkoff.kora.guide.dependencyinjection.messenger.slack;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.guide.dependencyinjection.messenger.Messenger;

@Tag(SlackMessenger.class)
@Component
public final class SlackMessenger implements Messenger {

    @Override
    public void sendMessage(String message) {
        System.out.println("Slack: " + message);
    }
}