package io.koraframework.example.openapi.http.client;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import io.koraframework.example.openapi.petV2.api.PetApi;

@Root
@Component
public final class RootService {

    private final io.koraframework.example.openapi.petV2.api.PetApi petApiV2;
    private final io.koraframework.example.openapi.petV3.api.PetApi petApiV3;

    public RootService(PetApi petApiV2, io.koraframework.example.openapi.petV3.api.PetApi petApiV3) {
        this.petApiV2 = petApiV2;
        this.petApiV3 = petApiV3;
    }
}
