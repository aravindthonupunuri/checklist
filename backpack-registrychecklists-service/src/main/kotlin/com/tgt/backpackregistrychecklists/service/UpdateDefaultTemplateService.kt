package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.transport.ChecklistResponseTO
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import com.tgt.lists.common.components.exception.ErrorCode
import mu.KotlinLogging
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateDefaultTemplateService(
    @Inject private val checklistTemplateRepository: ChecklistTemplateRepository,
    @Inject private val registryChecklistRepository: RegistryChecklistRepository,
    @Inject private val getRegistryChecklistsService: GetRegistryChecklistsService
) {
    private val logger = KotlinLogging.logger { UpdateDefaultTemplateService::class.java.name }
    fun updateDefaultTemplateId(
        guestId: String,
        registryId: UUID,
        templateId: Int,
        channel: RegistryChannel,
        subChannel: RegistrySubChannel
    ): Mono<ChecklistResponseTO> {
        return checklistTemplateRepository.countByTemplateId(templateId)
        .flatMap {
            if (it < 1L)
                throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("No checklist exists for the given templateId - $templateId")))
            else {
                registryChecklistRepository.find(registryId)
                .switchIfEmpty {
                    throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("RegistryId-$registryId doesn't have an active checklist")))
                }
                .flatMap {
                    registryChecklistRepository.update(RegistryChecklist(registryId, templateId, LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value))
                    .flatMap {
                        getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel)
                    }
                }
            }
        }
    }
}
