package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.transport.ChecklistTemplateTO
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistTemplateResponseTO
import com.tgt.backpackregistryclient.util.RegistryType
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChecklistTemplatesService(
    @Inject private val checklistTemplateRepository: ChecklistTemplateRepository
) {
    fun getTemplatesForRegistryType(
        registryType: RegistryType
    ): Mono<RegistryChecklistTemplateResponseTO> {
        return checklistTemplateRepository.findDistinctTemplateId(registryType)
            .collectList()
            .map {
                    val checklistTemplateList = it.map { checkListTemplate ->
                    ChecklistTemplateTO(checkListTemplate.checklistTemplatePK.templateId, checkListTemplate.checklistName)
                }
                RegistryChecklistTemplateResponseTO(registryType, checklistTemplateList)
            }
    }
}
