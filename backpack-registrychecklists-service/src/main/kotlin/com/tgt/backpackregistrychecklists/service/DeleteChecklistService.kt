package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.transport.kafka.model.DeleteChecklistActionEvent
import com.tgt.backpackregistrychecklists.util.EventPublisher
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import com.tgt.lists.common.components.exception.ErrorCode
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteChecklistService(
    @Inject val checklistTemplateRepository: ChecklistTemplateRepository,
    @Inject val eventPublisher: EventPublisher
) {
    fun deleteChecklist(
        guestId: String,
        templateId: Int
    ): Mono<Void> {
        return checklistTemplateRepository.deleteByTemplateId(templateId)
            .flatMap {
                if (it == 0)
                    throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("checklist templateId is not found to delete")))

                eventPublisher.publishEvent(
                    DeleteChecklistActionEvent.getEventType(),
                    DeleteChecklistActionEvent(guestId, templateId),
                    guestId
                )
            }.then()
    }
}
