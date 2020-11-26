package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.util.EventPublisher
import com.tgt.lists.common.components.exception.BadRequestException
import org.apache.kafka.clients.producer.RecordMetadata
import reactor.core.publisher.Mono
import spock.lang.Specification

class DeleteChecklistServiceUnitTest extends Specification {

    DeleteChecklistService deleteChecklistService
    ChecklistTemplateRepository checklistTemplateRepository
    EventPublisher eventPublisher

    def setup() {
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        eventPublisher = Mock(EventPublisher)
        deleteChecklistService = new DeleteChecklistService(checklistTemplateRepository, eventPublisher)
    }

    def "test delete checklists service integrity"() {
        def recordMetadata = GroovyMock(RecordMetadata)

        when:
        deleteChecklistService.deleteChecklist("1234", 1).block()

        then:
        1 * checklistTemplateRepository.deleteByTemplateId(1) >> Mono.just(1)
        1 * eventPublisher.publishEvent(_,_,_) >> Mono.just(recordMetadata)
    }

    def "test delete checklists service when deleteByTemplateId gives error"() {
        def recordMetadata = GroovyMock(RecordMetadata)

        when:
        deleteChecklistService.deleteChecklist("1234",1).block()

        then:
        1 * checklistTemplateRepository.deleteByTemplateId(1) >> Mono.error(new InternalError())
        0 * eventPublisher.publishEvent(_,_,_) >> Mono.just(recordMetadata)
        thrown(InternalError)
    }

    def "test delete checklists service when checklist templateId to delete is not found"() {
        def recordMetadata = GroovyMock(RecordMetadata)

        when:
        deleteChecklistService.deleteChecklist("1234",1).block()

        then:
        1 * checklistTemplateRepository.deleteByTemplateId(1) >> Mono.just(0)
        0 * eventPublisher.publishEvent(_,_,_) >> Mono.just(recordMetadata)
        thrown(BadRequestException)
    }
}
