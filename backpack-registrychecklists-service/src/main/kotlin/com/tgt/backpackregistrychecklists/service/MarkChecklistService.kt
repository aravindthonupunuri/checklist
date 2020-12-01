package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistRequestTO
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkChecklistService(
    @Inject private val checkedSubCategoriesRepository: CheckedSubCategoriesRepository,
    @Inject private val registryChecklistRepository: RegistryChecklistRepository,
    @Inject private val checklistTemplateRepository: ChecklistTemplateRepository
) {

    fun markChecklistId(
        registryId: UUID,
        checklistId: Int,
        registryChecklistRequest: RegistryChecklistRequestTO,
        subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistResponseTO> {
        return registryChecklistRepository.find(registryId)
            .switchIfEmpty {
                throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("No templateId found for the given registryId")))
            }
            .flatMap {
                if (it.templateId == registryChecklistRequest.templateId) {
                    checklistTemplateRepository.findByTemplateIdAndChecklistId(registryChecklistRequest.templateId, checklistId)
                        .switchIfEmpty {
                            throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("No checklistId found for the given templateId")))
                        }
                        .flatMap { checklistTemplate ->
                            if (checklistTemplate.checklistTemplatePK.checklistId == checklistId) {
                                checkedSubCategoriesRepository.save(CheckedSubCategories(CheckedSubCategoriesId(registryId = registryId, templateId = registryChecklistRequest.templateId, checklistId = checklistId),
                                    createdUser = subChannel.value, updatedUser = subChannel.value))
                                    .map {
                                        RegistryChecklistResponseTO(registryId = registryId, isChecked = true, checklistId = checklistId, templateId = registryChecklistRequest.templateId)
                                    }
                            } else
                                throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("Not a valid templateId - checklistId combination")))
                        }
                } else {
                    throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("Not a valid registryId - templateId combination")))
                }
            }
    }
}
