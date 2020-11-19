package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.lists.common.components.exception.BadRequestException
import reactor.core.publisher.Mono
import spock.lang.Specification

class UnmarkChecklistServiceTest extends Specification {

    UnmarkChecklistService unmarkChecklistService
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository

    def setup() {
        registryChecklistSubCategoryRepository = Mock(CheckedSubCategoriesRepository)
        unmarkChecklistService = new UnmarkChecklistService(registryChecklistSubCategoryRepository)
    }

    def "test unMarkChecklistId - happy path"() {
        def registryId = UUID.randomUUID()
        def checklistId = "201"
        def templateId = 2

        when:
        def actual = unmarkChecklistService.unmarkChecklistId(registryId, checklistId, templateId).block()

        then:
        1 * registryChecklistSubCategoryRepository.delete(_) >> Mono.just(1)

        actual.registryId == registryId
        actual.checklistId == checklistId
        actual.templateId == templateId
        !actual.checked
    }

    def "test unMarkChecklistId - if checklistId/templateId doesn't exist"() {
        given:
        def registryId = UUID.randomUUID()
        def checklistId = "201"
        def templateId = 2

        when:
        unmarkChecklistService.unmarkChecklistId(registryId, checklistId, templateId).block()

        then:
        1 * registryChecklistSubCategoryRepository.delete(_) >> Mono.just(0)

        thrown(BadRequestException)
    }

    def "test unmarkChecklistId - Exception from database"() {
        given:
        def registryId = UUID.randomUUID()
        def checklistId = "201"
        def templateId = 2

        when:
        unmarkChecklistService.unmarkChecklistId(registryId, checklistId, templateId).block()

        then:
        1 * registryChecklistSubCategoryRepository.delete(_) >> Mono.error(new RuntimeException())

        thrown(RuntimeException)
    }
}
