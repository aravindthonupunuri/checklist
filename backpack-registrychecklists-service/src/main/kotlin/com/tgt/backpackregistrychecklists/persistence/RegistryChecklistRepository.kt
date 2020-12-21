package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import io.micronaut.data.annotation.Id
import reactor.core.publisher.Mono
import java.util.*

interface RegistryChecklistRepository {
    fun save(registryChecklist: RegistryChecklist): Mono<RegistryChecklist>
    fun deleteByTemplateId(templateId: Int): Mono<Int>
    fun find(@Id registryId: UUID): Mono<RegistryChecklist>
    fun update(registryCheckList: RegistryChecklist): Mono<RegistryChecklist>
}
