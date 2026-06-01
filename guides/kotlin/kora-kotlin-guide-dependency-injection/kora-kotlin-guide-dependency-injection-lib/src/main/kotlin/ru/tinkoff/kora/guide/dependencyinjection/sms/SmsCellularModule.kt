package ru.tinkoff.kora.guide.dependencyinjection.sms

import ru.tinkoff.kora.common.DefaultComponent

interface SmsCellularModule {

    @DefaultComponent
    fun smsCellularProvider(): SmsCellularProvider {
        return SmsCellularProvider { "1" }
    }
}
