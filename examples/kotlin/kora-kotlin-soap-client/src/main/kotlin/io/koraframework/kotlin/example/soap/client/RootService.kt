package io.koraframework.kotlin.example.soap.client

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.example.generated.soap.SimpleService

@Root
@Component
class RootService(private val service: SimpleService)
