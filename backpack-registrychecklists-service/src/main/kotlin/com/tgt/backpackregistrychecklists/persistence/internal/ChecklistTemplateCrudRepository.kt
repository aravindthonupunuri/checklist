package com.tgt.backpackregistrychecklists.persistence.internal

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ChecklistTemplateCrudRepository : ReactiveStreamsCrudRepository<ChecklistTemplate, ChecklistTemplatePK>, ChecklistTemplateRepository
