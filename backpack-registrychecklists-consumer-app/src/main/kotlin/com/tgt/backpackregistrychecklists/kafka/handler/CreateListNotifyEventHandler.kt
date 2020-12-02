package com.tgt.backpackregistrychecklists.kafka.handler

import com.tgt.backpackregistrychecklists.service.async.DefaultChecklistService
import com.tgt.lists.atlas.kafka.model.CreateListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateListNotifyEventHandler(
    @Inject private val defaultChecklistService: DefaultChecklistService,
    @Inject private val eventHeaderFactory: EventHeaderFactory
) {

    private val logger = KotlinLogging.logger { CreateListNotifyEventHandler::class.java.name }

    fun handleCreateRegistryNotifyEvent(
        createListNotifyEvent: CreateListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<EventProcessingResult> {
        return defaultChecklistService.addDefaultTemplateIdToRegistry(
            createListNotifyEvent.listId,
            createListNotifyEvent.listSubType!!,
            createListNotifyEvent.subChannel!!)
            .map {
                EventProcessingResult(true, eventHeaders, createListNotifyEvent)
            }
            .onErrorResume {
                logger.debug("CreateListNotifyEvent didn't complete, adding it to DLQ for retry")
                val message = "Error from handleCreateRegistryNotifyEvent() for guest: " +
                    "${createListNotifyEvent.guestId} with registryId: ${createListNotifyEvent.listId}"
                val retryHeader = eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders,
                    errorCode = 500, errorMsg = message)
                Mono.just(EventProcessingResult(false, retryHeader, createListNotifyEvent))
            }
    }
}
