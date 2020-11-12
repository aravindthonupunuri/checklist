package com.tgt.backpackregistrychecklists.persistence.internal

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface CheckedSubcategoriesCrudRepository : ReactiveStreamsCrudRepository<CheckedSubCategories, CheckedSubCategoriesId>, CheckedSubCategoriesRepository
