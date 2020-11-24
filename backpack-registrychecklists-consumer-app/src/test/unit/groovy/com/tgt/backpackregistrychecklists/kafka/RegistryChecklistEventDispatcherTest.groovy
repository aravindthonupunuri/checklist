package com.tgt.backpackregistrychecklists.kafka

import com.tgt.backpackregistry.kafka.RegistryChecklistEventDispatcher
import com.tgt.backpackregistrychecklists.kafka.handler.DeleteChecklistActionEventHandler
import com.tgt.backpackregistrychecklists.transport.kafka.model.DeleteChecklistActionEvent
import com.tgt.lists.msgbus.event.EventHeaders
import spock.lang.Shared
import spock.lang.Specification

class RegistryChecklistEventDispatcherTest extends Specification {
    DeleteChecklistActionEventHandler deleteChecklistActionEventHandler
    RegistryChecklistEventDispatcher registryChecklistEventDispatcher

    @Shared
    String source = "Valid Source"

    @Shared
    String dlqSource = "Valid DLQ Source"

    def setup() {
        deleteChecklistActionEventHandler = Mock(DeleteChecklistActionEventHandler)
        registryChecklistEventDispatcher = new RegistryChecklistEventDispatcher
            (deleteChecklistActionEventHandler, source, dlqSource, [source, dlqSource])
    }

    def "Event available with invalid source - transformValue method"() {
        given:
        EventHeaders eventHeaders = new EventHeaders(
            UUID.randomUUID(),
            "coid",
            DeleteChecklistActionEvent.eventType,
            0,
            10L,
            1,
            0,
            "",
            100L,
            100L,
            "Invalid Source",
            false
        )

        when:
        def transformedValue = registryChecklistEventDispatcher.transformValue(eventHeaders, new byte[10])

        then:
        transformedValue == null
    }

    def "Event available with invalid source, event is ignored returning back success - dispatchEvent method"() {
        given:
        EventHeaders eventHeaders = new EventHeaders(
            UUID.randomUUID(),
            "coid",
            DeleteChecklistActionEvent.eventType,
            0,
            10L,
            1,
            0,
            "",
            100L,
            100L,
            "Invalid Source",
            false
        )

        when:
        def transformedValue = registryChecklistEventDispatcher.dispatchEvent(eventHeaders, new byte[10], false).block()

        then:
        transformedValue.success
    }
}
