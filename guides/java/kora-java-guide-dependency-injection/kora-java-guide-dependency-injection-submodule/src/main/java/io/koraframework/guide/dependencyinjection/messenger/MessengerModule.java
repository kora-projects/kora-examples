package io.koraframework.guide.dependencyinjection.messenger;

import io.koraframework.common.annotation.KoraSubmodule;

@KoraSubmodule
public interface MessengerModule {

    final class MessengerTag {
        private MessengerTag() {}
    }
}