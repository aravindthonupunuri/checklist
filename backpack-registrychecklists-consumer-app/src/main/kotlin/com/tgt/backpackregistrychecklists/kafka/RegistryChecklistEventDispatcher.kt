package com.tgt.backpackregistrychecklists.kafka

import com.tgt.backpackregistrychecklists.kafka.handler.CreateListNotifyEventHandler
import com.tgt.backpackregistrychecklists.kafka.handler.DeleteChecklistActionEventHandler
import com.tgt.backpackregistrychecklists.transport.kafka.model.DeleteChecklistActionEvent
import com.tgt.lists.atlas.kafka.model.CreateListNotifyEvent
import com.tgt.lists.msgbus.EventDispatcher
import com.tgt.lists.msgbus.event.DeadEventTransformedValue
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import com.tgt.lists.msgbus.event.EventTransformedValue
import com.tgt.lists.msgbus.execution.ExecutionSerialization
import io.micronaut.context.annotation.Value
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class RegistryChecklistEventDispatcher(
    @Inject private val deleteChecklistActionEventHandler: DeleteChecklistActionEventHandler,
    @Inject private val createListNotifyEventHandler: CreateListNotifyEventHandler,
    @Value("\${msgbus.source}") private val source: String,
    @Value("\${msgbus.dlq-source}") private val dlqSource: String,
    @Value("\${kafka-sources.allow}") val allowedSources: List<String>
) : EventDispatcher {

    private val logger = KotlinLogging.logger {}

    override fun handleDlqDeadEvent(eventHeaders: EventHeaders, data: ByteArray): DeadEventTransformedValue? {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Transform ByteArray data to a concrete type based on event type header
     * It is also used by msgbus framework during dql publish exception handling
     */
    override fun transformValue(eventHeaders: EventHeaders, data: ByteArray): EventTransformedValue? {
        if (eventHeaders.source == source || eventHeaders.source == dlqSource || allowedSources.contains(eventHeaders.source)) {
            return when (eventHeaders.eventType) {
                DeleteChecklistActionEvent.getEventType() -> {
                    val deleteChecklistActionEvent = DeleteChecklistActionEvent.deserialize(data)
                    EventTransformedValue("template_${deleteChecklistActionEvent.templateId}", ExecutionSerialization.ID_SERIALIZATION, deleteChecklistActionEvent)
                }
                CreateListNotifyEvent.getEventType() -> {
                    val createListNotifyEvent = CreateListNotifyEvent.deserialize(data)
                    EventTransformedValue("guest_${createListNotifyEvent.guestId}", ExecutionSerialization.ID_SERIALIZATION, createListNotifyEvent)
                }
                else -> null
            }
        }
        return null
    }

    override fun dispatchEvent(eventHeaders: EventHeaders, data: Any, isPoisonEvent: Boolean): Mono<EventProcessingResult> {
        if (eventHeaders.source == source || eventHeaders.source == dlqSource || allowedSources.contains(eventHeaders.source)) {
            when (eventHeaders.eventType) {
                DeleteChecklistActionEvent.getEventType() -> {
                    // always use transformValue to convert raw data to concrete type
                    val deleteChecklistActionEvent = data as DeleteChecklistActionEvent
                    logger.debug { "Got DeleteChecklist Action Event: $deleteChecklistActionEvent" }
                    return deleteChecklistActionEventHandler.handleDeleteChecklistActionEvent(deleteChecklistActionEvent, eventHeaders, isPoisonEvent)
                }
                CreateListNotifyEvent.getEventType() -> {
                    // always use transformValue to convert raw data to concrete type
                    val createListNotifyEvent = data as CreateListNotifyEvent
                    logger.debug { "Got CreateList Event: $createListNotifyEvent" }
                    return createListNotifyEventHandler.handleCreateRegistryNotifyEvent(createListNotifyEvent, eventHeaders, isPoisonEvent)
                }
            }

            logger.debug { "Unhandled eventType: ${eventHeaders.eventType}" }
            return Mono.just(EventProcessingResult(true, eventHeaders, data))
        }

        logger.debug { "Unhandled eventType: ${eventHeaders.eventType}" }
        return Mono.just(EventProcessingResult(true, eventHeaders, data))
    }
}
