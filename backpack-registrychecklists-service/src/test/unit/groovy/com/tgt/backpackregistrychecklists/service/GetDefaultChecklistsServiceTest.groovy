package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import reactor.core.publisher.Flux
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate

class GetDefaultChecklistsServiceTest extends Specification {

    GetDefaultChecklistsService getDefaultChecklistsService
    ChecklistTemplateRepository checklistTemplateRepository

    @Shared
    RegistryChannel channel = RegistryChannel.WEB
    @Shared
    RegistrySubChannel subChannel = RegistrySubChannel.TGTWEB
    @Shared
    ChecklistTemplate checklistTemplate1
    @Shared
    ChecklistTemplate checklistTemplate2
    @Shared
    ChecklistTemplate checklistTemplate3

    def setup() {
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        getDefaultChecklistsService = new GetDefaultChecklistsService(checklistTemplateRepository)
    }

    def setupSpec() {
        checklistTemplate1 = new ChecklistTemplate(new ChecklistTemplatePK(1, 201), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5xtjw", "5xtjw", "travel system", 1, "name", "taxonomyUrl", "reg_type=baby", LocalDate.now(), LocalDate.now())
        checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK(1, 202), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5xtk7", "5xtk7", "stroller", 2, "name", "taxonomyUrl", "reg_type=baby", LocalDate.now(), LocalDate.now())
        checklistTemplate3 = new ChecklistTemplate(new ChecklistTemplatePK(1, 203), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5q0ev", "5q0ev", "infant car seat", 2, "name", "taxonomyUrl", "reg_type=baby", LocalDate.now(), LocalDate.now())
    }

    def "test get default checklist info for a registryType - integrity"() {
        given:
        def registryType = RegistryType.BABY

        when:
        def result = getDefaultChecklistsService.getDefaultChecklistsForRegistryType(registryType, channel, subChannel).block()

        then:
        1 * checklistTemplateRepository.findByRegistryTypeAndDefaultChecklist(RegistryType.BABY, true) >> Flux.just(checklistTemplate1, checklistTemplate2, checklistTemplate3)
        result.categories.first().subcategories.size() == 3
        result.checklistTotalCount == 3
    }

    def "test get default checklist info for a invalid registryType"() {
        given:
        def registryType = RegistryType.CUSTOM

        when:
        getDefaultChecklistsService.getDefaultChecklistsForRegistryType(registryType, channel, subChannel).block()

        then:
        1 * checklistTemplateRepository.findByRegistryTypeAndDefaultChecklist(registryType, true) >> Flux.empty()
        thrown(BadRequestException)
    }
}
