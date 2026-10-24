package io.koraframework.guide.dependencyinjection.sms

import io.koraframework.common.annotation.DefaultComponent

interface SmsCellularModule {

    @DefaultComponent
    fun smsCellularProvider(): SmsCellularProvider {
        return SmsCellularProvider { "1" }
    }
}
