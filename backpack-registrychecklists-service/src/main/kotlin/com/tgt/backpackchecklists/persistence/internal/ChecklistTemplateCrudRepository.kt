package com.tgt.backpackchecklists.persistence.internal

import com.tgt.backpackchecklists.model.ChecklistTemplatePK
import com.tgt.backpackchecklists.model.ChecklistTemplate
import com.tgt.backpackchecklists.persistence.ChecklistTemplateRepository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ChecklistTemplateCrudRepository : ReactiveStreamsCrudRepository<ChecklistTemplate, ChecklistTemplatePK>, ChecklistTemplateRepository
