package com.tgt.backpackregistrychecklists.api


import com.tgt.backpackregistrychecklists.kafka.service.MigrationCheckListMarkNotifyEventService
import com.tgt.backpackregistrychecklists.service.MarkChecklistService
import com.tgt.backpackregistrychecklists.service.UnmarkChecklistService
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import reactor.core.publisher.Mono
import spock.lang.Specification

class MigrationCheckListMarkNotifyEventServiceTest extends Specification {

    MarkChecklistService markChecklistService
    UnmarkChecklistService unmarkChecklistService
    MigrationCheckListMarkNotifyEventService migrationCheckListMarkNotifyEventService

    def setup() {
        markChecklistService = Mock(MarkChecklistService)
        unmarkChecklistService = Mock(UnmarkChecklistService)
        migrationCheckListMarkNotifyEventService = new MigrationCheckListMarkNotifyEventService(markChecklistService, unmarkChecklistService)
    }

    def "test integrity"() {
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2
        RegistryChecklistResponseTO registryChecklistResponseTO = new RegistryChecklistResponseTO(registryId, true, checklistId, templateId)

        when:
        def actual = migrationCheckListMarkNotifyEventService.processMigrationCheckListMarkNotifyEvent(registryId, true, checklistId, templateId, RegistrySubChannel.KIOSK, new  MigrationCheckListMarkNotifyEventService.RetryState(false)).block()

        then:
        1 * markChecklistService.markChecklistId(_,_,_,_) >> Mono.just(registryChecklistResponseTO)

        actual.markCheckList
    }

    def "test exception"() {
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2

        when:
        def actual = migrationCheckListMarkNotifyEventService.processMigrationCheckListMarkNotifyEvent(registryId, true, checklistId, templateId, RegistrySubChannel.KIOSK, new  MigrationCheckListMarkNotifyEventService.RetryState(false)).block()

        then:
        1 * markChecklistService.markChecklistId(_,_,_,_) >> Mono.error(new RuntimeException())

        !actual.markCheckList
    }


}
