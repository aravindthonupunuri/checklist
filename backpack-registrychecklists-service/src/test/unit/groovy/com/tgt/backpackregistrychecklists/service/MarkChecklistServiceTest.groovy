package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistRequestTO
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import reactor.core.publisher.Mono
import spock.lang.Specification
import java.time.LocalDateTime

class MarkChecklistServiceTest extends Specification {

    MarkChecklistService markChecklistService
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository
    RegistryChecklistRepository registryChecklistRepository
    ChecklistTemplateRepository checklistTemplateRepository

    def setup() {
        registryChecklistSubCategoryRepository = Mock(CheckedSubCategoriesRepository)
        registryChecklistRepository = Mock(RegistryChecklistRepository)
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        markChecklistService = new MarkChecklistService(registryChecklistSubCategoryRepository, registryChecklistRepository, checklistTemplateRepository)
    }

    def "test markChecklistId - happy path"() {
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2
        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, null, RegistrySubChannel.KIOSK.value,
            null, RegistrySubChannel.KIOSK.value)

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, templateId, 1),
            "firstChecklistName", true, checklistId, "name", "name", "name",
            "1", "name", "subcategory_child_ids", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

        CheckedSubCategories registryChecklistSubCategory = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, templateId, checklistId),
            LocalDateTime.now(), RegistrySubChannel.KIOSK.value, LocalDateTime.now(), RegistrySubChannel.KIOSK.value)

        when:
        def actual = markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest, RegistrySubChannel.KIOSK).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateIdAndChecklistId(templateId, checklistId) >> Mono.just(checklistTemplate)
        1 * registryChecklistSubCategoryRepository.save(_) >> Mono.just(registryChecklistSubCategory)

        actual.registryId == registryId
        actual.checklistId == checklistId
        actual.checked
    }

    def "test markChecklistId - No templateId found for given registryId"() {
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2
        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        when:
        markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest, RegistrySubChannel.KIOSK).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.empty()

        def error = thrown(BadRequestException)
        error.message.contains("No templateId found for the given registryId")
    }

    def "test markChecklistId - No checklistId found for given templateId"() {
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2
        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, null, RegistrySubChannel.KIOSK.value,
            null, RegistrySubChannel.KIOSK.value)

        when:
        markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest, RegistrySubChannel.KIOSK).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateIdAndChecklistId(templateId, checklistId) >> Mono.empty()

        def error = thrown(BadRequestException)
        error.message.contains("No checklistId found for the given templateId")
    }

    def "test markChecklistId - Provided RegistryId - TemplateId combination is not valid"() {
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2
        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, 3, null, RegistrySubChannel.KIOSK.value,
            null, RegistrySubChannel.KIOSK.value)

        when:
        markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest, RegistrySubChannel.KIOSK).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)

        def error = thrown(BadRequestException)
        error.message.contains("Not a valid registryId - templateId combination")
    }

    def "test markChecklistId - Provided TemplateId - ChecklistId combination is not valid"() {
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2
        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, null, RegistrySubChannel.KIOSK.value,
            null, RegistrySubChannel.KIOSK.value)

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, templateId, 1),
            "firstChecklistName", true, 202, "name", "name", "name",
            "1", "name", "subcategory_child_ids", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

        when:
        markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest, RegistrySubChannel.KIOSK).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateIdAndChecklistId(templateId, checklistId) >> Mono.just(checklistTemplate)

        def error = thrown(BadRequestException)
        error.message.contains("Not a valid templateId - checklistId combination")
    }

    def "test markChecklistId - Exception from database"() {

        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2
        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        when:
        markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest, RegistrySubChannel.KIOSK).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.error(new RuntimeException())

        thrown(RuntimeException)
    }
}
