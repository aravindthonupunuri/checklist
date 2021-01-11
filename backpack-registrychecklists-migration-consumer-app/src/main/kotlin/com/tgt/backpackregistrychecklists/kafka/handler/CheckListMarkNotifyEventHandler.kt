package com.tgt.backpackregistrychecklists.kafka.handler

import com.tgt.backpackregistrychecklists.kafka.model.CheckListMarkNotifyEvent
import com.tgt.backpackregistrychecklists.kafka.service.MigrationCheckListMarkNotifyEventService
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckListMarkNotifyEventHandler(
    @Inject private val migrationCheckListMarkNotifyEventService: MigrationCheckListMarkNotifyEventService,
    @Inject private val eventHeaderFactory: EventHeaderFactory
) {
    private val logger = KotlinLogging.logger { CheckListMarkNotifyEventHandler::class.java.name }

    fun handleChecklistMarkNotifyEvent(
        checkListMarkNotifyEvent: CheckListMarkNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<EventProcessingResult> {
        val processingState: MigrationCheckListMarkNotifyEventService.RetryState = if (checkListMarkNotifyEvent.retryState != null) {
            MigrationCheckListMarkNotifyEventService.RetryState.deserialize(checkListMarkNotifyEvent.retryState.toString())
        } else {
            MigrationCheckListMarkNotifyEventService.RetryState()
        }

        return migrationCheckListMarkNotifyEventService.processMigrationCheckListMarkNotifyEvent(
            registryId = checkListMarkNotifyEvent.listId,
            checked = checkListMarkNotifyEvent.checked,
            checklistId = checkListMarkNotifyEvent.checklistId,
            templateId = checkListMarkNotifyEvent.templateId,
            subChannel = checkListMarkNotifyEvent.subChannel,
            retryState = processingState
        ).map {
            if (it.completeState()) {
                logger.debug("checkListMarkNotifyEvent processing is complete")
                EventProcessingResult(true, eventHeaders, checkListMarkNotifyEvent)
            } else {
                logger.debug("checkListMarkNotifyEvent didn't complete, adding it to DLQ for retry")
                val message = "Error from handleChecklistMarkNotifyEvent() for registry id: ${checkListMarkNotifyEvent.listId}, checked: ${checkListMarkNotifyEvent.checklistId}, template Id: ${checkListMarkNotifyEvent.templateId} and checklist id: ${checkListMarkNotifyEvent.checklistId}"
                    val retryHeader = eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message)
                checkListMarkNotifyEvent.retryState = MigrationCheckListMarkNotifyEventService.RetryState.serialize(it)
                    EventProcessingResult(false, retryHeader, checkListMarkNotifyEvent)
                }
            }
        }
}
