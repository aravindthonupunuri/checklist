package com.tgt.backpackregistrychecklists.kafka.handler

import com.tgt.backpackregistrychecklists.service.async.ChecklistService
import com.tgt.backpackregistrychecklists.transport.kafka.model.DeleteChecklistActionEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteChecklistActionEventHandler(
    @Inject private val checklistService: ChecklistService,
    @Inject private val eventHeaderFactory: EventHeaderFactory
) {
    private val logger = KotlinLogging.logger { DeleteChecklistActionEventHandler::class.java.name }
    fun handleDeleteChecklistActionEvent(
        deleteChecklistActionEvent: DeleteChecklistActionEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<EventProcessingResult> {

        val processingState: ChecklistService.RetryState = if (deleteChecklistActionEvent.retryState != null) {
            ChecklistService.RetryState.deserialize(deleteChecklistActionEvent.retryState.toString())
        } else {
            ChecklistService.RetryState(
                deleteChecklistTemplateFromChecklistRepository = false,
                deleteChecklistTemplateFromCheckedSubCategoriesRepository = false)
        }
        return checklistService.processDeleteRetryState(deleteChecklistActionEvent.templateId, processingState)
            .map {
                if (it.completeState()) {
                    logger.debug("DeleteChecklistActionEvent processing is complete")
                    EventProcessingResult(true, eventHeaders, deleteChecklistActionEvent)
                } else {
                    logger.debug("DeleteChecklistActionEvent didn't complete, adding it to DLQ for retry")
                    val message = "Error from handleDeleteChecklistActionEvent() for template: " +
                        "${deleteChecklistActionEvent.templateId}"
                    val retryHeader = eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders,
                        errorCode = 500, errorMsg = message)
                    deleteChecklistActionEvent.retryState = ChecklistService.RetryState.serialize(it)
                    EventProcessingResult(false, retryHeader, deleteChecklistActionEvent)
                }
            }
    }
}
