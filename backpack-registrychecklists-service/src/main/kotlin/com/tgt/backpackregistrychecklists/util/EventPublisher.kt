package com.tgt.backpackregistrychecklists.util

import com.tgt.lists.msgbus.EventType
import com.tgt.lists.msgbus.ListsMessageBusProducer
import mu.KotlinLogging
import org.apache.kafka.clients.producer.RecordMetadata
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventPublisher(
    @Inject private val listsMessageBusProducer: ListsMessageBusProducer<String, Any>
) {

    private val logger = KotlinLogging.logger {}

    fun publishEvent(eventType: EventType, message: Any, partitionKey: String): Mono<RecordMetadata> {
        return listsMessageBusProducer.sendMessage(eventType, message, partitionKey)
            .doOnError { logger.error("Failed to publish kafka event for EventType $eventType with message $message to message bus topic kafka topic") }
    }
}
