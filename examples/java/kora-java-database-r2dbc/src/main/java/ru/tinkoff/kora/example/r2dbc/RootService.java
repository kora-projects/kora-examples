package ru.tinkoff.kora.example.r2dbc;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final R2dbcCrudRepository r2dbcCrudRepository;
    private final R2dbcIdRandomRepository r2dbcIdRandomRepository;
    private final R2dbcIdSequenceRepository r2dbcIdSequenceRepository;
    private final R2dbcMapperColumnRepository r2dbcMapperColumnRepository;
    private final R2dbcMapperRowRepository r2dbcMapperRowRepository;
    private final R2dbcMapperParameterRepository r2dbcMapperParameterRepository;
    private final R2dbcCrudMacrosRepository r2dbcCrudMacrosRepository;
    private final R2dbcCrudSyncRepository r2dbcCrudSyncRepository;

    public RootService(R2dbcCrudRepository r2dbcCrudRepository,
                       R2dbcIdRandomRepository r2dbcIdRandomRepository,
                       R2dbcIdSequenceRepository r2dbcIdSequenceRepository,
                       R2dbcMapperColumnRepository r2dbcMapperColumnRepository,
                       R2dbcMapperRowRepository r2dbcMapperRowRepository,
                       R2dbcMapperParameterRepository r2dbcMapperParameterRepository,
                       R2dbcCrudMacrosRepository r2dbcCrudMacrosRepository,
                       R2dbcCrudSyncRepository r2dbcCrudSyncRepository) {
        this.r2dbcCrudRepository = r2dbcCrudRepository;
        this.r2dbcIdRandomRepository = r2dbcIdRandomRepository;
        this.r2dbcIdSequenceRepository = r2dbcIdSequenceRepository;
        this.r2dbcMapperColumnRepository = r2dbcMapperColumnRepository;
        this.r2dbcMapperRowRepository = r2dbcMapperRowRepository;
        this.r2dbcMapperParameterRepository = r2dbcMapperParameterRepository;
        this.r2dbcCrudMacrosRepository = r2dbcCrudMacrosRepository;
        this.r2dbcCrudSyncRepository = r2dbcCrudSyncRepository;
    }
}
