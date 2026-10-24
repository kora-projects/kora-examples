package io.koraframework.example.jdbc;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final JdbcCrudSyncRepository jdbcCrudSyncRepository;
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
    private final JdbcCrudExtendedRepository jdbcCrudExtendedRepository;
    private final JdbcCrudExtendedCompositeRepository jdbcCrudExtendedCompositeRepository;

    public RootService(JdbcCrudSyncRepository jdbcCrudSyncRepository,
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
                       JdbcCrudMacrosIdCompositeRepository jdbcCrudMacrosIdCompositeRepository,
                       JdbcCrudExtendedRepository jdbcCrudExtendedRepository,
                       JdbcCrudExtendedCompositeRepository jdbcCrudExtendedCompositeRepository) {
        this.jdbcCrudSyncRepository = jdbcCrudSyncRepository;
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
        this.jdbcCrudExtendedRepository = jdbcCrudExtendedRepository;
        this.jdbcCrudExtendedCompositeRepository = jdbcCrudExtendedCompositeRepository;
    }
}
