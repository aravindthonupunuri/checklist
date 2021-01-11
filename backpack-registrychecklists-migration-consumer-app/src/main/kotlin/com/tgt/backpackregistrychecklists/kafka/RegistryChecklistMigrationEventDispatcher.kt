package com.tgt.backpackregistrychecklists.kafka

import com.tgt.backpackregistrychecklists.kafka.handler.CheckListMarkNotifyEventHandler
import com.tgt.backpackregistrychecklists.kafka.model.CheckListMarkNotifyEvent
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
open class RegistryChecklistMigrationEventDispatcher(
    @Inject private val checkListMarkNotifyEventHandler: CheckListMarkNotifyEventHandler,
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
                CheckListMarkNotifyEvent.getEventType() -> {
                    val checkListMarkNotifyEvent = CheckListMarkNotifyEvent.deserialize(data)
                    EventTransformedValue("template_${checkListMarkNotifyEvent.templateId}", ExecutionSerialization.ID_SERIALIZATION, checkListMarkNotifyEvent)
                }

                else -> null
            }
        }
        return null
    }

    override fun dispatchEvent(eventHeaders: EventHeaders, data: Any, isPoisonEvent: Boolean): Mono<EventProcessingResult> {
        if (eventHeaders.source == source || eventHeaders.source == dlqSource || allowedSources.contains(eventHeaders.source)) {
            when (eventHeaders.eventType) {
                CheckListMarkNotifyEvent.getEventType() -> {
                    // always use transformValue to convert raw data to concrete type
                    val checkListMarkNotifyEvent = data as CheckListMarkNotifyEvent
                    logger.debug { "Got CheckListMarkNotifyEvent Event: $checkListMarkNotifyEvent" }
                    return checkListMarkNotifyEventHandler.handleChecklistMarkNotifyEvent(checkListMarkNotifyEvent, eventHeaders, isPoisonEvent)
                }
            }

            logger.debug { "Unhandled eventType: ${eventHeaders.eventType}" }
            return Mono.just(EventProcessingResult(true, eventHeaders, data))
        }

        logger.debug { "Unhandled eventType: ${eventHeaders.eventType}" }
        return Mono.just(EventProcessingResult(true, eventHeaders, data))
    }
}
