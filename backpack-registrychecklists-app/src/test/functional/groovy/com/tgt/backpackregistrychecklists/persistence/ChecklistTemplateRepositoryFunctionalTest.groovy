package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Stepwise
import javax.inject.Inject
import java.time.LocalDate
import java.util.stream.Collectors

@MicronautTest
@Stepwise
class ChecklistTemplateRepositoryFunctionalTest extends BasePersistenceFunctionalTest{

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    Logger LOG = LoggerFactory.getLogger(ChecklistTemplateRepositoryFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    def "test save checklist"() {
        given:
        def checklistTemplatePk = new ChecklistTemplatePK( 1, 101)
        def checklistTemplatePk1 = new ChecklistTemplatePK( 2, 101)
        def checklistTemplatePk2 = new ChecklistTemplatePK( 2, 201)
        def checklistTemplatePk3 = new ChecklistTemplatePK( 1, 201)
        def checklistTemplatePk4 = new ChecklistTemplatePK( 3, 101)
        def checklistTemplate = new ChecklistTemplate(checklistTemplatePk, RegistryType.BABY, "Baby1", true, 1, "categoryId", "name", "name", "1", "name", "subcategory_child_ids", 1, "name", "taxonomyUrl", "name", LocalDate.now(), LocalDate.now())
        def checklistTemplate1 = new ChecklistTemplate(checklistTemplatePk1, RegistryType.WEDDING, "Wedding1", true, 2, "categoryId", "name", "name", "1", "name", "subcategory_child_ids", 1, "name", "taxonomyUrl", "name", LocalDate.now(), LocalDate.now())
        def checklistTemplate2 = new ChecklistTemplate(checklistTemplatePk2, RegistryType.WEDDING, "Wedding2", true, 3, "categoryId", "name", "name", "1", "name", "subcategory_child_ids", 1, "name", "taxonomyUrl", "name", LocalDate.now(), LocalDate.now())
        def checklistTemplate3 = new ChecklistTemplate(checklistTemplatePk3, RegistryType.BABY, "Baby2", true, 4, "categoryId", "name", "name", "1", "name", "subcategory_child_ids", 1, "name", "taxonomyUrl", "name", LocalDate.now(), LocalDate.now())
        def checklistTemplate4 = new ChecklistTemplate(checklistTemplatePk4, RegistryType.BABY, "Baby3", false, 5, "categoryId", "name", "name", "1", "name", "subcategory_child_ids", 1, "name", "taxonomyUrl", "name", LocalDate.now(), LocalDate.now())

        when:
        def result = checklistTemplateRepository.save(checklistTemplate).block()
        def result1 = checklistTemplateRepository.save(checklistTemplate1).block()
        def result2 = checklistTemplateRepository.save(checklistTemplate2).block()
        def result3 = checklistTemplateRepository.save(checklistTemplate3).block()
        def result4 = checklistTemplateRepository.save(checklistTemplate4).block()

        then:
        result == checklistTemplate
        result1 != null
        result2 != null
        result3 != null
        result4 != null
    }

    def "test find checklist"() {

        when:
        def result = checklistTemplateRepository.countByRegistryTypeAndDefaultChecklist(RegistryType.WEDDING, true).block()
        def result1 = checklistTemplateRepository.countByRegistryTypeAndDefaultChecklist(RegistryType.BABY, true).block()
        def result2 = checklistTemplateRepository.countByRegistryTypeAndDefaultChecklist(RegistryType.BABY, false).block()

        then:
        result == 2
        result1 != null
        result2 == 1
    }

    def "test find default checklists for given registry type"() {

        when:
        def result = checklistTemplateRepository.findByDefaultChecklistAndRegistryType(true, RegistryType.BABY).collect(Collectors.toList()).block().size()

        then:
        result == 2
    }
    def "test find by default checklist"() {

        when:
        def result = checklistTemplateRepository.findByDefaultChecklistAndRegistryType(true, RegistryType.BABY).collect(Collectors.toList()).block().size()
        def result1 = checklistTemplateRepository.findByDefaultChecklistAndRegistryType(false, RegistryType.BABY).collect(Collectors.toList()).block().size()

        then:
        result == 2
        result1 == 1
    }

    def "test countByChecklistName"() {

        when:
        def result = checklistTemplateRepository.countByChecklistName("Wedding1").block()

        then:
        result == 1
    }

    def "test find by templateId"() {

        when:
        def result = checklistTemplateRepository.findByTemplateId(1).collect(Collectors.toList()).block()
        def result1 = checklistTemplateRepository.findByTemplateId(11).collect(Collectors.toList()).block()

        then:
        result1 == []
        result.size() == 2
        result.get(0).registryType == RegistryType.BABY
        result.get(0).getChecklistTemplatePK().templateId == 1
    }

    def "test findDistinctTemplateId"() {

        when:
        def result = checklistTemplateRepository.findDistinctTemplateId(RegistryType.BABY).collect(Collectors.toList()).block()

        then:
        result.size() == 2
        result.get(0).registryType == RegistryType.BABY
        result.get(0).getChecklistTemplatePK().templateId == 1
        result.get(0).categoryOrder == 1
        result.get(1).getChecklistTemplatePK().templateId == 3
        result.get(1).categoryOrder == 5
    }

    def " test findByTemplateIdAndChecklistId"() {

        when:
        def result = checklistTemplateRepository.findByTemplateIdAndChecklistId(1, 101).block()
        def result1 = checklistTemplateRepository.findByTemplateIdAndChecklistId(2, 101).block()
        def result2 = checklistTemplateRepository.findByTemplateIdAndChecklistId(2, 201).block()

        then:
        result != null
        result1 != null
        result.checklistName == "Baby1"
        result1.checklistName == "Wedding1"
        result2 != null
    }

    def "test delete checklist"() {

        when:
        def result = checklistTemplateRepository.deleteByTemplateId(1).block()
        def result1 = checklistTemplateRepository.deleteByTemplateId( 2).block()
        def result2 = checklistTemplateRepository.deleteByTemplateId( 50).block()

        then:
        result == 2
        result1 != null
        result1 == 2
        result2 == 0
    }
}
