package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Stepwise
import javax.inject.Inject
import java.time.LocalDate
import java.util.stream.Collectors

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
    Integer checklistId

    def setupSpec() {
        registryId = UUID.randomUUID()
        templateId = 2
        checklistId = 200
    }

    def "test save"() {

        LocalDate date = LocalDate.now()
        def checkedSubCategories = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, templateId, checklistId), date, "null", date, "null")
        def checkedSubCategories2 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, templateId, 201), date, "null", date, "null")
        def checkedSubCategories3 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 3, 202), date, "null", date, "null")
        def checkedSubCategories4 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, templateId, 203), date, "null", date, "null")

        when:
        def actual = registryChecklistSubCategoryRepository.save(checkedSubCategories).block()
        def actual2 = registryChecklistSubCategoryRepository.save(checkedSubCategories2).block()
        def actual3 = registryChecklistSubCategoryRepository.save(checkedSubCategories3).block()
        def actual4 = registryChecklistSubCategoryRepository.save(checkedSubCategories4).block()

        then:
        actual != null
        actual2 != null
        actual3 != null
        actual4 != null
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
    }

    def "test findByRegistryIdAndTemplateId"() {
        when:
        def actual = registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, templateId).collect(Collectors.toList()).block()

        then:
        actual != null
        actual.size() == 2
        actual.get(0).checkedSubcategoriesId.templateId == templateId
        actual.get(0).checkedSubcategoriesId.registryId == registryId
        actual.get(0).checkedSubcategoriesId.checklistId == 201
        actual.get(1).checkedSubcategoriesId.checklistId == 203
    }
}
