package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.lib.api.util.AppErrorCodes
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
        checklistId: String,
        templateId: Int
    ): Mono<RegistryChecklistResponseTO> {
        return checkedSubCategoriesRepository.delete(CheckedSubCategoriesId(registryId, templateId, checklistId))
            .map {
                if (it == 1)
                    RegistryChecklistResponseTO(registryId = registryId, isChecked = false, checklistId = checklistId, templateId = templateId)
                else
                    throw BadRequestException(AppErrorCodes.BAD_REQUEST_ERROR_CODE(listOf("No record found in database for the provided registryId/checklistId combination")))
            }
    }
}
