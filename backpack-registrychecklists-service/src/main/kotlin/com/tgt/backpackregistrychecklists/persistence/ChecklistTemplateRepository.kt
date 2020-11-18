package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.data.annotation.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChecklistTemplateRepository {
    fun save(checkList: ChecklistTemplate): Mono<ChecklistTemplate>
    @Query("""SELECT * FROM checklist_template where registry_type=(:registryType)""")
    fun find(registryType: RegistryType): Flux<ChecklistTemplate>
    fun findByTemplateId(templateId: Int): Flux<ChecklistTemplate>
    @Query("""DELETE FROM checklist_template where template_id=(:templateId)""")
    fun deleteByTemplateId(templateId: Int): Mono<Int>
    fun update(checkList: ChecklistTemplate): Mono<ChecklistTemplate>
    @Query("SELECT Distinct on (template_id) template_id, * FROM CHECKLIST_TEMPLATE WHERE REGISTRY_TYPE = :registryType")
    fun findDistinctTemplateId(registryType: RegistryType): Flux<ChecklistTemplate>
}
