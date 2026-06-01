package ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller

import ru.tinkoff.kora.common.Principal

data class DataApiPrincipal(val name: String) : Principal
