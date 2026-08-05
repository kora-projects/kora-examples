package io.koraframework.guide.dependencyinjection.messenger.slack;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.guide.dependencyinjection.messenger.Messenger;

@Tag(SlackMessenger.class)
@Component
public final class SlackMessenger implements Messenger {

    @Override
    public void sendMessage(String message) {
        System.out.println("Slack: " + message);
    }
}