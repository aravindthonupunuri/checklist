package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.data.annotation.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChecklistTemplateRepository {
    fun save(checkList: ChecklistTemplate): Mono<ChecklistTemplate>
    fun findByRegistryTypeAndDefaultChecklist(registryType: RegistryType, default: Boolean = true): Flux<ChecklistTemplate>
    fun countByRegistryTypeAndDefaultChecklist(registryType: RegistryType, default: Boolean = true): Mono<Long>
    fun findByDefaultChecklistAndRegistryType(defaultChecklist: Boolean, registryType: RegistryType): Flux<ChecklistTemplate>
    fun findByTemplateId(templateId: Int): Flux<ChecklistTemplate>
    fun findByTemplateIdAndChecklistId(templateId: Int, checklistId: Int): Mono<ChecklistTemplate>
    fun countByTemplateId(templateId: Int): Mono<Long>
    fun countByChecklistName(checkListName: String): Mono<Long>
    fun deleteByTemplateId(templateId: Int): Mono<Int>
    @Query("SELECT Distinct on (template_id) template_id, * FROM CHECKLIST_TEMPLATE WHERE REGISTRY_TYPE = :registryType")
    fun findDistinctTemplateId(registryType: RegistryType): Flux<ChecklistTemplate>
}
