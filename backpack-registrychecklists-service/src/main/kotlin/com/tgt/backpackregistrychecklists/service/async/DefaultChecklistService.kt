package com.tgt.backpackregistrychecklists.service.async

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import com.tgt.lists.common.components.exception.ErrorCode
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultChecklistService(
    @Inject val checklistTemplateRepository: ChecklistTemplateRepository,
    @Inject val registryChecklistRepository: RegistryChecklistRepository
) {
    fun addDefaultTemplateIdToRegistry(
        registryId: UUID,
        listSubType: String,
        subChannel: String
    ): Mono<Boolean> {
        return if (listOf(RegistryType.WEDDING, RegistryType.BABY).contains(RegistryType.toRegistryType(listSubType))) {
            checklistTemplateRepository.findByDefaultChecklistAndRegistryType(true, RegistryType.toRegistryType(listSubType))
                .switchIfEmpty<ChecklistTemplate> {
                    throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("no subcategory with default value of true is found")))
                }.collectList().flatMap {
                    val registryChecklist = RegistryChecklist(registryId, it.first().checklistTemplatePK.templateId, LocalDate.now(), subChannel, LocalDate.now(), subChannel)
                    registryChecklistRepository.save(registryChecklist).map { true }
                }
        } else Mono.just(true)
    }
}
