package ru.tinkoff.kora.guide.dependencyinjection.messenger;

import ru.tinkoff.kora.common.KoraSubmodule;

@KoraSubmodule
public interface MessengerModule {

    final class MessengerTag {
        private MessengerTag() {}
    }
}