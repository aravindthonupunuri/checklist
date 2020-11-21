package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.test.annotation.MicronautTest
import org.slf4j.Logger
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.LocalDateTime
import java.util.stream.Collectors

@MicronautTest
@Stepwise
class ChecklistTemplateRepositoryFunctionalTest extends BasePersistenceFunctionalTest{

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    @Override
    Logger getLogger() {
        return null
    }

    def "test save checklist"() {
        given:
        def checklistTemplatePk = new ChecklistTemplatePK(RegistryType.BABY, 1, 1)
        def checklistTemplatePk1 = new ChecklistTemplatePK(RegistryType.WEDDING, 2, 1)
        def checklistTemplatePk2 = new ChecklistTemplatePK(RegistryType.WEDDING, 2, 2)
        def checklistTemplatePk3 = new ChecklistTemplatePK(RegistryType.BABY, 1, 2)
        def checklistTemplatePk4 = new ChecklistTemplatePK(RegistryType.BABY, 3, 1)
        def checklistTemplate = new ChecklistTemplate(checklistTemplatePk, "Baby1", true, 1, "categoryId", "name", "name", "1", "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate1 = new ChecklistTemplate(checklistTemplatePk1, "Wedding1", true, 2, "categoryId", "name", "name", "1", "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate2 = new ChecklistTemplate(checklistTemplatePk2, "Wedding2", true, 3, "categoryId", "name", "name", "1", "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate3 = new ChecklistTemplate(checklistTemplatePk3, "Baby2", true, 4, "categoryId", "name", "name", "1", "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate4 = new ChecklistTemplate(checklistTemplatePk4, "Baby3", true, 5, "categoryId", "name", "name", "1", "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

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
        def result = checklistTemplateRepository.countByRegistryType(RegistryType.WEDDING).block()
        def result1 = checklistTemplateRepository.countByRegistryType(RegistryType.BABY).block()

        then:
        result == 2
        result1 != null
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
        result.get(0).getChecklistTemplatePK().registryType == RegistryType.BABY
        result.get(0).getChecklistTemplatePK().templateId == 1
    }

    def "test findDistinctTemplateId"() {

        when:
        def result = checklistTemplateRepository.findDistinctTemplateId(RegistryType.BABY).collect(Collectors.toList()).block()

        then:
        result.size() == 2
        result.get(0).getChecklistTemplatePK().registryType == RegistryType.BABY
        result.get(0).getChecklistTemplatePK().templateId == 1
        result.get(0).getChecklistTemplatePK().categoryOrder == 1
        result.get(1).getChecklistTemplatePK().templateId == 3
        result.get(1).getChecklistTemplatePK().categoryOrder == 1
    }

    def " test findByTemplateIdAndChecklistId"() {

        when:
        def result = checklistTemplateRepository.findByTemplateIdAndChecklistId(1, 1).block()
        def result1 = checklistTemplateRepository.findByTemplateIdAndChecklistId(1, 4).block()
        def result2 = checklistTemplateRepository.findByTemplateIdAndChecklistId(2, 4).block()

        then:
        result != null
        result1 != null
        result.checklistName == "Baby1"
        result1.checklistName == "Baby2"
        result2 == null
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
