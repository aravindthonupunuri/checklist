package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import com.tgt.lists.common.components.exception.ErrorCode
import reactor.core.publisher.Mono
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnmarkChecklistService(
    @Inject private val checkedSubCategoriesRepository: CheckedSubCategoriesRepository
) {
    fun unmarkChecklistId(
        registryId: UUID,
        checklistId: Int,
        templateId: Int
    ): Mono<RegistryChecklistResponseTO> {
        return checkedSubCategoriesRepository.delete(CheckedSubCategoriesId(registryId, templateId, checklistId))
            .map {
                if (it == 1)
                    RegistryChecklistResponseTO(registryId = registryId, checked = false, checklistId = checklistId, templateId = templateId)
                else
                    throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("No record found in database for the provided registryId/checklistId combination")))
            }
    }
}
