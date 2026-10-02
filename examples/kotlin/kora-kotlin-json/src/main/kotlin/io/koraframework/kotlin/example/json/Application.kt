package io.koraframework.kotlin.example.json

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.json.common.JsonModule

@KoraApp
interface Application : JsonModule

fun main() = KoraApplication.run { ApplicationGraph.graph() }
