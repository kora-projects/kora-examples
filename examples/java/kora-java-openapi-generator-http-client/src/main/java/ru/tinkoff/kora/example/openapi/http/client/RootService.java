package ru.tinkoff.kora.example.openapi.http.client;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.example.openapi.petV2.api.PetApi;

@Root
@Component
public final class RootService {

    private final ru.tinkoff.kora.example.openapi.petV2.api.PetApi petApiV2;
    private final ru.tinkoff.kora.example.openapi.petV3.api.PetApi petApiV3;

    public RootService(PetApi petApiV2, ru.tinkoff.kora.example.openapi.petV3.api.PetApi petApiV3) {
        this.petApiV2 = petApiV2;
        this.petApiV3 = petApiV3;
    }
}
