package ru.tinkoff.kora.kotlin.example.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.*
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcResultColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcRowMapper

@Root
@Component
class RootService(
    private val r2dbcCrudSuspendRepository: R2dbcCrudSuspendRepository,
    private val r2dbcCrudSyncRepository: R2dbcCrudSyncRepository,
    private val r2dbcCrudMacrosRepository: R2dbcCrudMacrosRepository,
    private val r2dbcIdRandomRepository: R2dbcIdRandomRepository,
    private val r2dbcIdSequenceRepository: R2dbcIdSequenceRepository,
    private val r2dbcMapperColumnRepository: R2dbcMapperColumnRepository,
    private val r2dbcMapperParameterRepository: R2dbcMapperParameterRepository,
    private val r2dbcMapperRowRepository: R2dbcMapperRowRepository
)


