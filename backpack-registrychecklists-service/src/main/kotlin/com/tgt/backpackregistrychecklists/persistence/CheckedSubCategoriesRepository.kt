package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import io.micronaut.data.annotation.Id
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface CheckedSubCategoriesRepository {

    fun save(registry: CheckedSubCategories): Mono<CheckedSubCategories>
    fun delete(@Id checkedSubcategoriesId: CheckedSubCategoriesId): Mono<Int>
    fun deleteByTemplateId(templateId: Int): Mono<Int>
    fun find(@Id checkedSubcategoriesId: CheckedSubCategoriesId): Mono<CheckedSubCategories>
    fun findByRegistryIdAndTemplateId(registryId: UUID, templateId: Int): Flux<CheckedSubCategories>
}
