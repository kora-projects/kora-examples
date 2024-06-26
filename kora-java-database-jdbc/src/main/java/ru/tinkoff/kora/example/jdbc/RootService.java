package ru.tinkoff.kora.example.jdbc;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final JdbcCrudSyncRepository jdbcCrudSyncRepository;
    private final JdbcCrudAsyncRepository jdbcCrudAsyncRepository;
    private final JdbcCrudReactorRepository jdbcCrudReactorRepository;
    private final JdbcMapperResultSetRepository jdbcMapperResultSetRepository;
    private final JdbcMapperRowRepository jdbcMapperRowRepository;
    private final JdbcMapperColumnRepository jdbcMapperColumnRepository;
    private final JdbcMapperParameterRepository jdbcMapperParameterRepository;
    private final JdbcIdSequenceRepository jdbcIdSequenceRepository;
    private final JdbcIdRandomRepository jdbcIdRandomRepository;
    private final JdbcIdSequenceCompositeRepository jdbcIdSequenceCompositeRepository;
    private final JdbcIdRandomCompositeRepository jdbcIdRandomCompositeRepository;
    private final JdbcJsonbRepository jdbcJsonbRepository;
    private final JdbcCrudMacrosRepository jdbcCrudMacrosRepository;
    private final JdbcCrudMacrosIdCompositeRepository jdbcCrudMacrosIdCompositeRepository;

    public RootService(JdbcCrudSyncRepository jdbcCrudSyncRepository,
                       JdbcCrudAsyncRepository jdbcCrudAsyncRepository,
                       JdbcCrudReactorRepository jdbcCrudReactorRepository,
                       JdbcMapperResultSetRepository jdbcMapperResultSetRepository,
                       JdbcMapperRowRepository jdbcMapperRowRepository,
                       JdbcMapperColumnRepository jdbcMapperColumnRepository,
                       JdbcMapperParameterRepository jdbcMapperParameterRepository,
                       JdbcIdSequenceRepository jdbcIdSequenceRepository,
                       JdbcIdRandomRepository jdbcIdRandomRepository,
                       JdbcIdSequenceCompositeRepository jdbcIdSequenceCompositeRepository,
                       JdbcIdRandomCompositeRepository jdbcIdRandomCompositeRepository,
                       JdbcJsonbRepository jdbcJsonbRepository,
                       JdbcCrudMacrosRepository jdbcCrudMacrosRepository,
                       JdbcCrudMacrosIdCompositeRepository jdbcCrudMacrosIdCompositeRepository) {
        this.jdbcCrudSyncRepository = jdbcCrudSyncRepository;
        this.jdbcCrudAsyncRepository = jdbcCrudAsyncRepository;
        this.jdbcCrudReactorRepository = jdbcCrudReactorRepository;
        this.jdbcMapperResultSetRepository = jdbcMapperResultSetRepository;
        this.jdbcMapperRowRepository = jdbcMapperRowRepository;
        this.jdbcMapperColumnRepository = jdbcMapperColumnRepository;
        this.jdbcMapperParameterRepository = jdbcMapperParameterRepository;
        this.jdbcIdSequenceRepository = jdbcIdSequenceRepository;
        this.jdbcIdRandomRepository = jdbcIdRandomRepository;
        this.jdbcIdSequenceCompositeRepository = jdbcIdSequenceCompositeRepository;
        this.jdbcIdRandomCompositeRepository = jdbcIdRandomCompositeRepository;
        this.jdbcJsonbRepository = jdbcJsonbRepository;
        this.jdbcCrudMacrosRepository = jdbcCrudMacrosRepository;
        this.jdbcCrudMacrosIdCompositeRepository = jdbcCrudMacrosIdCompositeRepository;
    }
}
