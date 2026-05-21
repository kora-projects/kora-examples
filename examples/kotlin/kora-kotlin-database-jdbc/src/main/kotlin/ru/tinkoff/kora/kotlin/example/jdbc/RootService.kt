package ru.tinkoff.kora.kotlin.example.jdbc

import jakarta.annotation.Nullable
import org.postgresql.util.PGobject
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.Module
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.*
import ru.tinkoff.kora.database.jdbc.EntityJdbc
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultSetMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcRowMapper
import ru.tinkoff.kora.json.common.JsonReader
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.common.annotation.Json
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

@Root
@Component
class RootService(
    private val jdbcCrudSyncRepository: JdbcCrudSyncRepository,
    private val jdbcCrudMacrosRepository: JdbcCrudMacrosRepository,
    private val jdbcIdRandomRepository: JdbcIdRandomRepository,
    private val jdbcIdSequenceRepository: JdbcIdSequenceRepository,
    private val jdbcIdRandomCompositeRepository: JdbcIdRandomCompositeRepository,
    private val jdbcIdSequenceCompositeRepository: JdbcIdSequenceCompositeRepository,
    private val jdbcCrudExtendedRepository: JdbcCrudExtendedRepository,
    private val jdbcCrudExtendedCompositeRepository: JdbcCrudExtendedCompositeRepository,
    private val jdbcCrudMacrosIdCompositeRepository: JdbcCrudMacrosIdCompositeRepository,
    private val jdbcCrudSuspendRepository: JdbcCrudSuspendRepository,
    private val jdbcMapperColumnRepository: JdbcMapperColumnRepository,
    private val jdbcMapperParameterRepository: JdbcMapperParameterRepository,
    private val jdbcMapperRowRepository: JdbcMapperRowRepository,
    private val jdbcMapperResultSetRepository: JdbcMapperResultSetRepository,
    private val jdbcJsonbRepository: JdbcJsonbRepository,
)


