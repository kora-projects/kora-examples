package ru.tinkoff.kora.guide.dependencyinjection.messenger

import ru.tinkoff.kora.common.KoraSubmodule

@KoraSubmodule
interface MessengerModule {

    class MessengerTag private constructor()
}
