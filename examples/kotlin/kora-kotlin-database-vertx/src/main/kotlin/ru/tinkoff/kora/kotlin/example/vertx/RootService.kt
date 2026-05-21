package ru.tinkoff.kora.kotlin.example.vertx

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.vertx.VertxRepository
import ru.tinkoff.kora.database.vertx.mapper.parameter.VertxParameterColumnMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxResultColumnMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxRowMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxRowSetMapper

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

