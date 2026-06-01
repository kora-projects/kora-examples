package ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller;

public final class RestrictedFormNameException extends RuntimeException {

    public RestrictedFormNameException(String name) {
        super("Form name '" + name + "' is restricted");
    }
}
