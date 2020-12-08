package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistryclient.util.RegistryType
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.time.LocalDate

class GetChecklistTemplatesServiceTest extends Specification {

    GetChecklistTemplatesService getChecklistTemplatesService
    ChecklistTemplateRepository checklistTemplateRepository

    def setup() {
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        getChecklistTemplatesService = new GetChecklistTemplatesService(checklistTemplateRepository)
    }

    def "test getChecklistTemplates - baby - single template"() {

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK( 1,
            101), RegistryType.BABY, "firstChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subcategory_child_ids", 1, "name", "name", LocalDate.now(), LocalDate.now())

        when:
        def actual = getChecklistTemplatesService.getTemplatesForRegistryType(RegistryType.BABY).block()

        then:
        1 * checklistTemplateRepository.findDistinctTemplateId(_) >> Flux.just(checklistTemplate)

        actual.registryType == RegistryType.BABY
        actual.checklists.size() == 1
        actual.checklists.get(0).checklistName == "firstChecklistName"
        actual.checklists.get(0).templateId == 1
    }

    def "test getChecklistTemplates - baby - multiple templates"() {

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK( 1,
            101), RegistryType.BABY, "firstChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subcategory_child_ids", 1, "name", "name", LocalDate.now(), LocalDate.now())

        ChecklistTemplate checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK( 2,
            1), RegistryType.BABY, "secondChecklistName", false, 1, "name", "name", "name",
            "1", "name", "subcategory_child_ids", 1, "name", "name", LocalDate.now(), LocalDate.now())

        when:
        def actual = getChecklistTemplatesService.getTemplatesForRegistryType(RegistryType.BABY).block()

        then:
        1 * checklistTemplateRepository.findDistinctTemplateId(_) >> Flux.just(checklistTemplate, checklistTemplate2)

        actual.registryType == RegistryType.BABY
        actual.checklists.size() == 2
        actual.checklists.get(0).checklistName == "firstChecklistName"
        actual.checklists.get(0).templateId == 1
        actual.checklists.get(1).checklistName == "secondChecklistName"
        actual.checklists.get(1).templateId == 2
    }

    def "test getChecklistTemplates - no templates for a registry type"() {
        when:
        def actual = getChecklistTemplatesService.getTemplatesForRegistryType(RegistryType.WEDDING).block()

        then:
        1 * checklistTemplateRepository.findDistinctTemplateId(_) >> Flux.empty()

        actual.registryType == RegistryType.WEDDING
        actual.checklists.size() == 0
    }

    def "test getChecklistTemplates - Exception from database"() {
        when:
        getChecklistTemplatesService.getTemplatesForRegistryType(RegistryType.WEDDING).block()

        then:
        1 * checklistTemplateRepository.findDistinctTemplateId(_) >> Flux.error(new RuntimeException())

        thrown(RuntimeException)
    }
}
