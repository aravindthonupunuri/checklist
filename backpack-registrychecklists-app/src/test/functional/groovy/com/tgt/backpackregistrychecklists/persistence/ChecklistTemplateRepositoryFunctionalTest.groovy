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
        def checklistTemplatePk1 = new ChecklistTemplatePK(RegistryType.WEDDING, 1, 1)
        def checklistTemplatePk2 = new ChecklistTemplatePK(RegistryType.WEDDING, 1, 2)
        def checklistTemplatePk3 = new ChecklistTemplatePK(RegistryType.BABY, 1, 2)
        def checklistTemplatePk4 = new ChecklistTemplatePK(RegistryType.BABY, 2, 1)
        def checklistTemplate = new ChecklistTemplate(checklistTemplatePk, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate1 = new ChecklistTemplate(checklistTemplatePk1, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate2 = new ChecklistTemplate(checklistTemplatePk2, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate3 = new ChecklistTemplate(checklistTemplatePk3, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate4 = new ChecklistTemplate(checklistTemplatePk4, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

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
        def result = checklistTemplateRepository.find(RegistryType.WEDDING).collect(Collectors.toList()).block()
        def result1 = checklistTemplateRepository.find(RegistryType.BABY).collect(Collectors.toList()).block()

        then:
        result.size() == 2
        result1 != null
    }

    def "test findDistinctTemplateId"() {

        when:
        def result = checklistTemplateRepository.findDistinctTemplateId(RegistryType.BABY).collect(Collectors.toList()).block()

        then:
        result.size() == 2
        result.get(0).getChecklistTemplatePK().registryType == RegistryType.BABY
        result.get(0).getChecklistTemplatePK().templateId == 1
        result.get(0).getChecklistTemplatePK().categoryOrder == 1
        result.get(1).getChecklistTemplatePK().templateId == 2
        result.get(1).getChecklistTemplatePK().categoryOrder == 1
    }

    def "test delete checklist"() {

        when:
        def result = checklistTemplateRepository.deleteByTemplateId(1).block()
        def result1 = checklistTemplateRepository.deleteByTemplateId( 1).block()

        then:
        result == 4
        result1 != null
    }
}
