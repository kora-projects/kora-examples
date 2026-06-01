package ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller

class RestrictedFormNameException(name: String) : RuntimeException("Form name '$name' is restricted")
