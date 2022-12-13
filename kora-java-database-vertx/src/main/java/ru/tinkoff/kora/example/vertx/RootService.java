package ru.tinkoff.kora.example.vertx;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final VertxCrudSyncRepository vertxCrudRepository;
    private final VertxMapperColumnRepository vertxMapperColumnRepository;
    private final VertxMapperParameterRepository vertxMapperParameterRepository;
    private final VertxMapperRowSetRepository vertxMapperRowSetRepository;
    private final VertxMapperRowRepository vertxMapperRowRepository;
    private final VertxCrudReactorRepository vertxCrudReactorRepository;

    public RootService(VertxCrudSyncRepository vertxCrudRepository,
                       VertxMapperColumnRepository vertxMapperColumnRepository,
                       VertxMapperParameterRepository vertxMapperParameterRepository,
                       VertxMapperRowSetRepository vertxMapperRowSetRepository,
                       VertxMapperRowRepository vertxMapperRowRepository,
                       VertxCrudReactorRepository vertxCrudReactorRepository) {
        this.vertxCrudRepository = vertxCrudRepository;
        this.vertxMapperColumnRepository = vertxMapperColumnRepository;
        this.vertxMapperParameterRepository = vertxMapperParameterRepository;
        this.vertxMapperRowSetRepository = vertxMapperRowSetRepository;
        this.vertxMapperRowRepository = vertxMapperRowRepository;
        this.vertxCrudReactorRepository = vertxCrudReactorRepository;
    }
}
