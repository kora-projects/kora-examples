package io.koraframework.kotlin.example.config.hocon

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root

@Root
@Component
class RootService(private val fooConfig: FooConfig)
