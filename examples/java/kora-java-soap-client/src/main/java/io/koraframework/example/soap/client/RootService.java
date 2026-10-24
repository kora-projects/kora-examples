package io.koraframework.example.soap.client;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import io.koraframework.example.generated.soap.SimpleService;

@Root
@Component
public final class RootService {

    private final SimpleService service;

    public RootService(SimpleService service) {
        this.service = service;
    }
}
