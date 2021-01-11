package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import com.tgt.lists.common.components.exception.ErrorCode
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
        templateId: Int,
        subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistResponseTO> {
        return registryChecklistRepository.find(registryId)
            .switchIfEmpty {
                throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("No templateId found for the given registryId")))
            }
            .flatMap {
                if (it.templateId == templateId) {
                    checklistTemplateRepository.findByTemplateIdAndChecklistId(templateId, checklistId)
                        .switchIfEmpty {
                            throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("Not a valid checklistId - templateId combination")))
                        }
                        .flatMap {
                            checkedSubCategoriesRepository.save(
                                CheckedSubCategories(
                                    checkedSubcategoriesId = CheckedSubCategoriesId(
                                        registryId = registryId,
                                        templateId = templateId,
                                        checklistId = checklistId
                                    ),
                                    createdUser = subChannel.value,
                                    updatedUser = subChannel.value
                                )
                            )
                        }.map {
                            RegistryChecklistResponseTO(
                                registryId = registryId,
                                checked = true,
                                checklistId = checklistId,
                                templateId = templateId
                            )
                        }
                } else {
                    throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("Not a valid registryId - templateId combination")))
                }
            }
    }
}
