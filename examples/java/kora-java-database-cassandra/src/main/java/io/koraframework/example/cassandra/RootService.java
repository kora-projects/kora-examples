package io.koraframework.example.cassandra;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final CassandraCrudSyncRepository crudSyncRepository;
    private final CassandraCrudAsyncRepository crudAsyncRepository;
    private final CassandraMapperResultSetRepository mapperResultSetRepository;
    private final CassandraMapperRowRepository mapperRowRepository;
    private final CassandraMapperRowColumnRepository mapperRowColumnRepository;
    private final CassandraMapperParameterRepository mapperParameterRepository;
    private final CassandraUdtRepository cassandraUdtRepository;

    public RootService(CassandraCrudSyncRepository crudSyncRepository,
                       CassandraCrudAsyncRepository crudAsyncRepository,
                       CassandraMapperResultSetRepository mapperResultSetRepository,
                       CassandraMapperRowRepository mapperRowRepository,
                       CassandraMapperRowColumnRepository mapperRowColumnRepository,
                       CassandraMapperParameterRepository mapperParameterRepository,
                       CassandraUdtRepository cassandraUdtRepository) {
        this.crudSyncRepository = crudSyncRepository;
        this.crudAsyncRepository = crudAsyncRepository;
        this.mapperResultSetRepository = mapperResultSetRepository;
        this.mapperRowRepository = mapperRowRepository;
        this.mapperRowColumnRepository = mapperRowColumnRepository;
        this.mapperParameterRepository = mapperParameterRepository;
        this.cassandraUdtRepository = cassandraUdtRepository;
    }
}
