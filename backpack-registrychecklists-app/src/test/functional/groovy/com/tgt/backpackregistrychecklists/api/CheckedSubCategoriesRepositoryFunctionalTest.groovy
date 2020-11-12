package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import io.micronaut.test.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.LocalDateTime

@MicronautTest
@Stepwise
class CheckedSubCategoriesRepositoryFunctionalTest extends BasePersistenceFunctionalTest {

    Logger LOG = LoggerFactory.getLogger(CheckedSubCategoriesRepositoryFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository

    @Shared
    UUID registryId

    @Shared
    Integer templateId

    @Shared
    String checklistId

    def setupSpec() {
        registryId = UUID.randomUUID()
        templateId = 2
        checklistId = "200"
    }

    def "test save"() {

        LocalDateTime date = LocalDateTime.now()
        def compositeKey = new CheckedSubCategoriesId(registryId, templateId, checklistId)
        def markChecklist = new CheckedSubCategories(compositeKey, date, "null", date, "null")

        when:
        def actual = registryChecklistSubCategoryRepository.save(markChecklist).block()

        then:
        actual != null
        actual.checkedSubcategoriesId.registryId == registryId
        actual.checkedSubcategoriesId.templateId == templateId
        actual.checkedSubcategoriesId.checklistId == checklistId
    }

    def "test delete"() {

        def compositeKey = new CheckedSubCategoriesId(registryId, templateId, checklistId)

        when:
        def actual = registryChecklistSubCategoryRepository.delete(compositeKey).block()

        then:
        actual != null
        actual == 1
    }

    def "test find"() {

        def compositeKey = new CheckedSubCategoriesId(registryId, templateId, checklistId)

        when:
        def actual = registryChecklistSubCategoryRepository.find(compositeKey).block()

        then:
        actual == null
//        actual.checkedSubcategoriesId.checklistId == checklistId
    }

}

