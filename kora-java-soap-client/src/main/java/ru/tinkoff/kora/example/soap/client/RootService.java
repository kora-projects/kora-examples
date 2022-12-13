package ru.tinkoff.kora.example.soap.client;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.example.generated.soap.SimpleService;

@Root
@Component
public final class RootService {

    private final SimpleService service;

    public RootService(SimpleService service) {
        this.service = service;
    }
}
