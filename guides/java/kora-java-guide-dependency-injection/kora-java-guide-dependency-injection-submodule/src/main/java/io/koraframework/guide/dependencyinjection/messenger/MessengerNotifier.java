package io.koraframework.guide.dependencyinjection.messenger;

import io.koraframework.application.graph.All;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.guide.dependencyinjection.common.Notifier;

@Tag(MessengerModule.MessengerTag.class)
@Component
public final class MessengerNotifier implements Notifier {

    private final All<Messenger> messengers;

    public MessengerNotifier(@Tag(Tag.Any.class) All<Messenger> messengers) {
        this.messengers = messengers;
    }

    @Override
    public void notify(String user, String message) {
        System.out.println("Broadcasting to messengers");
        for (var messenger : messengers) {
            messenger.sendMessage(user + "@" + message);
        }
        System.out.println("Messenger broadcast complete");
    }
}