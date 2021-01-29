package com.tgt.backpackregistrychecklists.kafka.handler

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.service.async.DefaultChecklistService
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDate

class CreateListNotifyEventHandlerUnitTest extends Specification {
    DefaultChecklistService defaultChecklistService
    ChecklistTemplateRepository checklistTemplateRepository
    RegistryChecklistRepository registryChecklistRepository

    def setup() {
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        registryChecklistRepository = Mock(RegistryChecklistRepository)
        defaultChecklistService = new DefaultChecklistService(checklistTemplateRepository, registryChecklistRepository)
    }

    def "test defaultchecklistService() integrity"() {
        given:
        UUID registryId = UUID.randomUUID()
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "checklistName", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, 1, LocalDate.now(), "user", LocalDate.now(), "user")

        when:
        def response = defaultChecklistService.addDefaultTemplateIdToRegistry(registryId, "BABY", RegistrySubChannel.KIOSK.name()).block()

        then:
        1 * checklistTemplateRepository.findByDefaultChecklistAndRegistryType(true, RegistryType.BABY) >> Flux.just(checklistTemplate)
        1 * registryChecklistRepository.save(_) >> Mono.just(registryChecklist)
    }

    def "test defaultchecklistService() when default value as true is not found"() {
        given:
        UUID registryId = UUID.randomUUID()
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "checklistName", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, 1, LocalDate.now(), "user", LocalDate.now(), "user")

        when:
        def response = defaultChecklistService.addDefaultTemplateIdToRegistry(registryId, "BABY", RegistrySubChannel.KIOSK.name()).block()

        then:
        1 * checklistTemplateRepository.findByDefaultChecklistAndRegistryType(true, RegistryType.BABY) >> Flux.empty()
        0 * registryChecklistRepository.save(_) >> Mono.just(registryChecklist)
        thrown(com.tgt.lists.common.components.exception.BadRequestException)
    }

    def "test defaultchecklistService() - when saving registry checkist in registryChecklist repository fails"() {
        given:
        UUID registryId = UUID.randomUUID()
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "checklistName", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, 1, LocalDate.now(), "user", LocalDate.now(), "user")

        when:
        def response = defaultChecklistService.addDefaultTemplateIdToRegistry(registryId, "BABY", RegistrySubChannel.KIOSK.name()).block()

        then:
        1 * checklistTemplateRepository.findByDefaultChecklistAndRegistryType(true, RegistryType.BABY) >> Flux.empty()
        0 * registryChecklistRepository.save(_) >> Mono.error(new RuntimeException())
        thrown(RuntimeException)
    }
}
