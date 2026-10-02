package io.koraframework.example.http.client;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final InterceptedHttpClient interceptedHttpClient;
    private final JsonHttpClient jsonHttpClient;
    private final MapperRequestHttpClient mapperRequestHttpClient;
    private final MapperResponseHttpClient mapperResponseHttpClient;
    private final ParametersHttpClient parametersHttpClient;
    private final VoidHttpClient voidHttpClient;
    private final FormHttpClient formHttpClient;

    public RootService(InterceptedHttpClient interceptedHttpClient,
                       JsonHttpClient jsonHttpClient,
                       MapperRequestHttpClient mapperRequestHttpClient,
                       MapperResponseHttpClient mapperResponseHttpClient,
                       ParametersHttpClient parametersHttpClient,
                       VoidHttpClient voidHttpClient,
                       FormHttpClient formHttpClient) {
        this.interceptedHttpClient = interceptedHttpClient;
        this.jsonHttpClient = jsonHttpClient;
        this.mapperRequestHttpClient = mapperRequestHttpClient;
        this.mapperResponseHttpClient = mapperResponseHttpClient;
        this.parametersHttpClient = parametersHttpClient;
        this.voidHttpClient = voidHttpClient;
        this.formHttpClient = formHttpClient;
    }
}
