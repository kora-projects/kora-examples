package ru.tinkoff.kora.example.cassandra;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final CassandraCrudSyncRepository crudSyncRepository;
    private final CassandraCrudAsyncRepository crudAsyncRepository;
    private final CassandraCrudReactorRepository crudReactorRepository;
    private final CassandraMapperResultSetRepository mapperResultSetRepository;
    private final CassandraMapperResultSetReactiveRepository mapperResultSetReactiveRepository;
    private final CassandraMapperRowRepository mapperRowRepository;
    private final CassandraMapperRowColumnRepository mapperRowColumnRepository;
    private final CassandraMapperParameterRepository mapperParameterRepository;
    private final CassandraUdtRepository cassandraUdtRepository;

    public RootService(CassandraCrudSyncRepository crudSyncRepository,
                       CassandraCrudAsyncRepository crudAsyncRepository,
                       CassandraCrudReactorRepository crudReactorRepository,
                       CassandraMapperResultSetRepository mapperResultSetRepository,
                       CassandraMapperResultSetReactiveRepository mapperResultSetReactiveRepository,
                       CassandraMapperRowRepository mapperRowRepository,
                       CassandraMapperRowColumnRepository mapperRowColumnRepository,
                       CassandraMapperParameterRepository mapperParameterRepository,
                       CassandraUdtRepository cassandraUdtRepository) {
        this.crudSyncRepository = crudSyncRepository;
        this.crudAsyncRepository = crudAsyncRepository;
        this.crudReactorRepository = crudReactorRepository;
        this.mapperResultSetRepository = mapperResultSetRepository;
        this.mapperResultSetReactiveRepository = mapperResultSetReactiveRepository;
        this.mapperRowRepository = mapperRowRepository;
        this.mapperRowColumnRepository = mapperRowColumnRepository;
        this.mapperParameterRepository = mapperParameterRepository;
        this.cassandraUdtRepository = cassandraUdtRepository;
    }
}
