package com.tgt.backpackregistrychecklists.persistence.internal

import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface RegistryChecklistCrudRepository : ReactiveStreamsCrudRepository<RegistryChecklist, UUID>, RegistryChecklistRepository
