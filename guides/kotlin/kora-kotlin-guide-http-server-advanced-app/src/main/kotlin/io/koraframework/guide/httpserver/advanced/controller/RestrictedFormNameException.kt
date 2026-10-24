package io.koraframework.guide.httpserver.advanced.controller

class RestrictedFormNameException(name: String) : RuntimeException("Form name '$name' is restricted")
