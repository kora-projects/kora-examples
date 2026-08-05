package io.koraframework.kotlin.example.jdbc

import org.postgresql.util.PGobject
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Module
import io.koraframework.common.annotation.Root
import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.*
import io.koraframework.database.jdbc.annotation.EntityJdbc
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import io.koraframework.database.jdbc.mapper.result.JdbcResultColumnMapper
import io.koraframework.database.jdbc.mapper.result.JdbcResultSetMapper
import io.koraframework.database.jdbc.mapper.result.JdbcRowMapper
import io.koraframework.json.common.JsonReader
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.annotation.Json
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


