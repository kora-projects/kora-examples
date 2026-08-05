package io.koraframework.guide.httpserver.advanced.controller;

public final class RestrictedFormNameException extends RuntimeException {

    public RestrictedFormNameException(String name) {
        super("Form name '" + name + "' is restricted");
    }
}
