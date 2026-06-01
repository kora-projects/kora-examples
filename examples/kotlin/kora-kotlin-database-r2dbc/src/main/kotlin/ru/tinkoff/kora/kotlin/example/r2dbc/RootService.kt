package ru.tinkoff.kora.kotlin.example.r2dbc

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root

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


