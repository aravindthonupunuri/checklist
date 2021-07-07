package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.transport.ChecklistCategoryTO
import com.tgt.backpackregistrychecklists.transport.DefaultChecklistResponseTO
import com.tgt.backpackregistrychecklists.transport.SubcategoryTO
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes
import com.tgt.lists.common.components.exception.ErrorCode
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDefaultChecklistsService(
    @Inject private val checklistTemplateRepository: ChecklistTemplateRepository
) {
    fun getDefaultChecklistsForRegistryType(
        registryType: RegistryType,
        channel: RegistryChannel,
        subChannel: RegistrySubChannel
    ): Mono<DefaultChecklistResponseTO> {
        return checklistTemplateRepository.findByRegistryTypeAndDefaultChecklist(registryType, true).collectList()
            .map { checklistTemplates ->
                if (checklistTemplates.isNullOrEmpty())
                    throw BadRequestException(ErrorCode(BaseErrorCodes.BAD_REQUEST_ERROR_CODE, listOf("No checklist exists for the given registryType - $registryType")))

                // Group by categoryId --> Map<categoryId, List<ChecklistTemplates>
                val categories = checklistTemplates.groupBy { it.categoryId }
                    .mapNotNull {
                        val checklistTemplate = it.value.first()
                        val subCategories = it.value.map { checklistTemplate -> SubcategoryTO(checklistTemplate) }
                        ChecklistCategoryTO(checklistTemplate!!, subCategories)
                    }

                DefaultChecklistResponseTO(
                    registryType = registryType,
                    defaultTemplateId = checklistTemplates.first().checklistTemplatePK.templateId,
                    categories = categories,
                    checklistTotalCount = categories.map { it.categoryTotalCount }.sum()
                )
            }
    }
}
