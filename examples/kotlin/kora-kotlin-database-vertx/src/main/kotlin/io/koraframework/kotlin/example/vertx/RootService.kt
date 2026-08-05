package io.koraframework.kotlin.example.vertx

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Root
import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Batch
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.vertx.VertxRepository
import io.koraframework.database.vertx.mapper.parameter.VertxParameterColumnMapper
import io.koraframework.database.vertx.mapper.result.VertxResultColumnMapper
import io.koraframework.database.vertx.mapper.result.VertxRowMapper
import io.koraframework.database.vertx.mapper.result.VertxRowSetMapper

@Root
@Component
class RootService(
    private val vertxCrudRepository: VertxCrudSyncRepository,
    private val vertxCrudSuspendRepository: VertxCrudSuspendRepository,
    private val vertxMapperColumnRepository: VertxMapperColumnRepository,
    private val vertxMapperParameterRepository: VertxMapperParameterRepository,
    private val vertxMapperRowRepository: VertxMapperRowRepository,
    private val vertxMapperRowSetRepository: VertxMapperRowSetRepository
)

