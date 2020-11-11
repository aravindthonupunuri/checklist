package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackchecklists.model.ChecklistTemplate
import com.tgt.backpackchecklists.model.ChecklistTemplatePK
import com.tgt.backpackchecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
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

    def "test save checklist"() {
        given:
        def checklistTemplatePk = new ChecklistTemplatePK("baby", 1, 1)
        def checklistTemplatePk1 = new ChecklistTemplatePK("wedding", 1, 1)
        def checklistTemplatePk2 = new ChecklistTemplatePK("wedding", 1, 2)
        def checklistTemplate = new ChecklistTemplate(checklistTemplatePk, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate1 = new ChecklistTemplate(checklistTemplatePk1, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())
        def checklistTemplate2 = new ChecklistTemplate(checklistTemplatePk2, "name", true, 1, "name", "name", "name", 1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

        when:
        def result = checklistTemplateRepository.save(checklistTemplate).block()
        def result1 = checklistTemplateRepository.save(checklistTemplate1).block()
        def result2 = checklistTemplateRepository.save(checklistTemplate2).block()

        then:
        result == checklistTemplate
        result1 != null
        result2 != null
    }

    def "test find checklist"() {

        when:
        def result = checklistTemplateRepository.find("wedding").collect(Collectors.toList()).block()
        def result1 = checklistTemplateRepository.find("baby").collectList().block()

        then:
        result.size() == 2
        result1 != null
    }

    def "test delete checklist"() {

        when:
        def result = checklistTemplateRepository.deleteByTemplateId(1).block()
        def result1 = checklistTemplateRepository.deleteByTemplateId( 1).block()

        then:
        result == 3
        result1 != null
    }

    @Override
    Logger getLogger() {
        return null
    }
}
