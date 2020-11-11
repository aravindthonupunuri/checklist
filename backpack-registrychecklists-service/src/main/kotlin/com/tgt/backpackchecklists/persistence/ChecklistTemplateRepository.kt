package com.tgt.backpackchecklists.persistence

import com.tgt.backpackchecklists.model.ChecklistTemplate
import io.micronaut.data.annotation.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChecklistTemplateRepository {
    fun save(checkList: ChecklistTemplate): Mono<ChecklistTemplate>
    @Query("""SELECT * FROM checklist_template where registry_type=(:registryType)""")
    fun find(registryType: String): Flux<ChecklistTemplate>
    fun findByTemplateId(templateId: Int): Flux<ChecklistTemplate>
    @Query("""DELETE FROM checklist_template where template_id=(:templateId)""")
    fun deleteByTemplateId(templateId: Int): Mono<Int>
    fun update(checkList: ChecklistTemplate): Mono<ChecklistTemplate>
}
