package com.tgt.backpackregistrychecklists.kafka.handler

import com.tgt.backpackregistrychecklists.service.async.ChecklistService
import com.tgt.backpackregistrychecklists.transport.kafka.model.DeleteChecklistActionEvent
import com.tgt.lists.atlas.kafka.model.CreateListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import reactor.core.publisher.Mono
import spock.lang.Shared
import spock.lang.Specification

class DeleteChecklistActionEventHandlerUnitTest extends Specification {
    ChecklistService checklistService
    EventHeaderFactory eventHeaderFactory
    EventHeaders eventHeaders
    DeleteChecklistActionEventHandler deleteChecklistActionEventHandler

    @Shared
    String dlqSource = "Valid DLQ Source"

    def setup() {
        eventHeaderFactory = new EventHeaderFactory( 2, 3, dlqSource)
        eventHeaders = new EventHeaders(
            UUID.randomUUID(),
            "coid",
            CreateListNotifyEvent.eventType,
            0,
            10L,
            1,
            0,
            "",
            100L,
            100L,
            "Valid Source",
            false
        )

        checklistService = Mock(ChecklistService)
        deleteChecklistActionEventHandler = new DeleteChecklistActionEventHandler(checklistService, eventHeaderFactory)
    }

    def "DeleteChecklistActionEvent is fired - registryChecklistRepository delete method fails"() {
        given:
        DeleteChecklistActionEvent deleteChecklistActionEvent = new DeleteChecklistActionEvent("1234", 1, null)

        def retryState = new  ChecklistService.RetryState(false, false)

        when:
        def handlerResponse = deleteChecklistActionEventHandler.handleDeleteChecklistActionEvent(deleteChecklistActionEvent, eventHeaders, false).block()

        then:
        1 * checklistService.processDeleteRetryState(_, _) >> Mono.just(retryState)

        !handlerResponse.success
        handlerResponse.eventHeaders.source == dlqSource
    }

    def "DeleteChecklistActionEvent is fired - checkedSubCategoriesRepository delete method fails"() {
        given:
        DeleteChecklistActionEvent deleteChecklistActionEvent = new DeleteChecklistActionEvent("1234", 1, null)

        def retryState = new  ChecklistService.RetryState(true, false)

        when:
        def handlerResponse = deleteChecklistActionEventHandler.handleDeleteChecklistActionEvent(deleteChecklistActionEvent, eventHeaders, false).block()

        then:
        1 * checklistService.processDeleteRetryState(_, _) >> Mono.just(retryState)

        !handlerResponse.success
        handlerResponse.eventHeaders.source == dlqSource
    }

    def "DeleteChecklistActionEvent is fired - both registryChecklistRepository checkedSubCategoriesRepository delete methods are successful"() {
        given:
        DeleteChecklistActionEvent deleteChecklistActionEvent = new DeleteChecklistActionEvent("1234",1, null)

        def retryState = new  ChecklistService.RetryState(true, true)

        when:
        def handlerResponse = deleteChecklistActionEventHandler.handleDeleteChecklistActionEvent(deleteChecklistActionEvent, eventHeaders, false).block()

        then:
        1 * checklistService.processDeleteRetryState(_, _) >> Mono.just(retryState)

        handlerResponse.success
    }
}
