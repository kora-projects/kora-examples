package io.koraframework.guide.openapi.httpserver.advanced.controller;

import io.koraframework.common.Principal;

public record DataApiPrincipal(String name) implements Principal {}


