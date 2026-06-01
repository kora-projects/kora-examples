package ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller;

import ru.tinkoff.kora.common.Principal;

public record DataApiPrincipal(String name) implements Principal {}


