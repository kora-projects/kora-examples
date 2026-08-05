package io.koraframework.guide.dependencyinjection.messenger

import io.koraframework.common.annotation.KoraSubmodule

@KoraSubmodule
interface MessengerModule {

    class MessengerTag private constructor()
}
