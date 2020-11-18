package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import io.micronaut.data.annotation.Id
import reactor.core.publisher.Mono

interface CheckedSubCategoriesRepository {

    fun save(registry: CheckedSubCategories): Mono<CheckedSubCategories>
    fun delete(@Id checkedSubcategoriesId: CheckedSubCategoriesId): Mono<Int>
    fun find(@Id checkedSubcategoriesId: CheckedSubCategoriesId): Mono<CheckedSubCategories>
//    @Query("SELECT * FROM CHECKED_SUBCATEGORIES WHERE registry_id = :registryId and checklist_id = :checklistId and template_id = :templateId", nativeQuery = true)
//    fun findWithNative(registryId: UUID, checklistId: String, templateId: Int): Mono<RegistryChecklistSubCategory>
}