package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.transport.*
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.lists.common.components.exception.BadRequestException
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDate

class UpdateDefaultTemplateServiceTest extends Specification {

    UpdateDefaultTemplateService updateDefaultTemplateService
    GetRegistryChecklistsService  getRegistryChecklistsService
    RegistryChecklistRepository registryChecklistRepository
    ChecklistTemplateRepository checklistTemplateRepository

    def setup(){
        registryChecklistRepository = Mock(RegistryChecklistRepository)
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        getRegistryChecklistsService = Mock(GetRegistryChecklistsService)
        updateDefaultTemplateService = new UpdateDefaultTemplateService(checklistTemplateRepository,
            registryChecklistRepository, getRegistryChecklistsService)
    }

    def "test update default template - integrity"() {
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def registryChecklist1 = new RegistryChecklist(registryId, 1, LocalDate.now(), RegistrySubChannel.KIOSK.value, LocalDate.now(), RegistrySubChannel.KIOSK.value)
        def registryChecklist = new RegistryChecklist(registryId, templateId, LocalDate.now(), RegistrySubChannel.KIOSK.value, LocalDate.now(), RegistrySubChannel.KIOSK.value)

        def responseTO = new ChecklistResponseTO(registryId, 1, [new ChecklistCategoryTO("963002", "strollers and car seats", 1,
            "category.image.url", [new SubcategoryTO(201, "5xtjw", "travel system", 1, "subcategory.image.url",
            "reg_type=baby", 1, true, new ItemDetailsTO("12954094", "Item Title", "primary.image.url", LocalDate.now(), LocalDate.now()))])])

        when:
        def result = updateDefaultTemplateService.updateDefaultTemplateId(guestId, registryId, registryChecklistRequest, RegistryChannel.WEB, RegistrySubChannel.KIOSK).block()

        then:
        1 * checklistTemplateRepository.countByTemplateId(_) >> Mono.just(1L)
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist1)
        1 * registryChecklistRepository.update(_) >> Mono.just(registryChecklist)
        1 * getRegistryChecklistsService.getChecklistsForRegistryId(*_) >> Mono.just(responseTO)

        result != null
        result.registryId == registryId
        result.registryItemCount == 1
        result.categories.size() == 1
        result.component3().get(0).categoryId == "963002"
        result.component3().get(0).subcategories.size() == 1
        result.component3().get(0).subcategories.get(0).checklistId == 201
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(0).subcategories.get(0).itemCount == 1
        result.component3().get(0).subcategories.get(0).lastUpdatedItem.tcin == "12954094"
    }

    def "test update default template - multiple categories"() {
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def registryChecklist1 = new RegistryChecklist(registryId, 1, LocalDate.now(), RegistrySubChannel.KIOSK.value, LocalDate.now(), RegistrySubChannel.KIOSK.value)
        def registryChecklist = new RegistryChecklist(registryId, templateId, LocalDate.now(), RegistrySubChannel.KIOSK.value, LocalDate.now(), RegistrySubChannel.KIOSK.value)

        def responseTO = new ChecklistResponseTO(registryId, 1, [new ChecklistCategoryTO("963002", "strollers and car seats", 1,
            "category.image.url", [new SubcategoryTO(201, "5xtjw", "travel system", 1, "subcategory.image.url",
            "reg_type=baby", 1, true, new ItemDetailsTO("12954094", "Item Title", "primary.image.url", LocalDate.now(), LocalDate.now()))]),
             new ChecklistCategoryTO("29504", "gear &amp; activity", 1,
                 "category.image.url", [new SubcategoryTO(208, "5q0eu", "baby carrier", 2, "subcategory.image.url",
                 "reg_type=baby", 0, true, null)])])

        when:
        def result = updateDefaultTemplateService.updateDefaultTemplateId(guestId, registryId, registryChecklistRequest, RegistryChannel.WEB, RegistrySubChannel.KIOSK).block()

        then:
        1 * checklistTemplateRepository.countByTemplateId(_) >> Mono.just(1L)
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist1)
        1 * registryChecklistRepository.update(_) >> Mono.just(registryChecklist)
        1 * getRegistryChecklistsService.getChecklistsForRegistryId(*_) >> Mono.just(responseTO)

        result != null
        result.registryId == registryId
        result.registryItemCount == 1
        result.categories.size() == 2
        result.component3().get(0).categoryId == "963002"
        result.component3().get(0).subcategories.size() == 1
        result.component3().get(0).subcategories.get(0).checklistId == 201
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(0).subcategories.get(0).itemCount == 1
        result.component3().get(0).subcategories.get(0).lastUpdatedItem.tcin == "12954094"
        result.component3().get(1).categoryId == "29504"
        result.component3().get(1).subcategories.size() == 1
        result.component3().get(1).subcategories.get(0).checklistId == 208
        result.component3().get(1).subcategories.get(0).subcategoryChildIds == "5q0eu"
        result.component3().get(1).subcategories.get(0).itemCount == 0
        result.component3().get(1).subcategories.get(0).lastUpdatedItem == null
    }

    def "test update default template - multiple subcategory childIds"() {
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def registryChecklist = new RegistryChecklist(registryId, templateId, LocalDate.now(), RegistrySubChannel.KIOSK.value, LocalDate.now(), RegistrySubChannel.KIOSK.value)
        def registryChecklist1 = new RegistryChecklist(registryId, 1, LocalDate.now(), RegistrySubChannel.KIOSK.value, LocalDate.now(), RegistrySubChannel.KIOSK.value)

        def responseTO = new ChecklistResponseTO(registryId, 1, [new ChecklistCategoryTO("963002", "strollers and car seats", 1,
            "category.image.url", [new SubcategoryTO(201, "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "stroller", 1, "subcategory.image.url",
            "reg_type=baby", 1, true, new ItemDetailsTO("12954094", "Item Title", "primary.image.url", LocalDate.now(), LocalDate.now()))]),
             new ChecklistCategoryTO("29504", "gear &amp; activity", 1,
                 "category.image.url", [new SubcategoryTO(208, "5q0eu", "baby carrier", 2, "subcategory.image.url",
                 "reg_type=baby", 0, true, null)])])

        when:
        def result = updateDefaultTemplateService.updateDefaultTemplateId(guestId, registryId, registryChecklistRequest, RegistryChannel.WEB, RegistrySubChannel.KIOSK).block()

        then:
        1 * checklistTemplateRepository.countByTemplateId(_) >> Mono.just(1L)
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist1)
        1 * registryChecklistRepository.update(_) >> Mono.just(registryChecklist)
        1 * getRegistryChecklistsService.getChecklistsForRegistryId(*_) >> Mono.just(responseTO)

        result != null
        result.registryId == registryId
        result.registryItemCount == 1
        result.categories.size() == 2
        result.component3().get(0).categoryId == "963002"
        result.component3().get(0).subcategories.size() == 1
        result.component3().get(0).subcategories.get(0).checklistId == 201
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u"
        result.component3().get(0).subcategories.get(0).itemCount == 1
        result.component3().get(0).subcategories.get(0).lastUpdatedItem.tcin == "12954094"
        result.component3().get(1).categoryId == "29504"
        result.component3().get(1).subcategories.size() == 1
        result.component3().get(1).subcategories.get(0).checklistId == 208
        result.component3().get(1).subcategories.get(0).subcategoryChildIds == "5q0eu"
        result.component3().get(1).subcategories.get(0).itemCount == 0
        result.component3().get(1).subcategories.get(0).lastUpdatedItem == null
    }

    def "test update default template - no checklist exists for the given templateId"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)

        when:
        updateDefaultTemplateService.updateDefaultTemplateId(guestId, registryId, registryChecklistRequest, RegistryChannel.WEB, RegistrySubChannel.KIOSK).block()

        then:
        1 * checklistTemplateRepository.countByTemplateId(_) >> Mono.just(0L)

        def error = thrown(BadRequestException)
        error.message.contains("No checklist exists for the given templateId - $templateId")
    }

    def "test update default template - if registryId doesn't have an active checklist"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)

        when:
        updateDefaultTemplateService.updateDefaultTemplateId(guestId, registryId, registryChecklistRequest, RegistryChannel.WEB, RegistrySubChannel.KIOSK).block()

        then:
        1 * checklistTemplateRepository.countByTemplateId(_) >> Mono.just(1L)
        1 * registryChecklistRepository.find(registryId) >> Mono.empty()

        def error = thrown(BadRequestException)
        error.message.contains("RegistryId-$registryId doesn't have an active checklist")
    }

    def "test update default template - exception from database"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)

        when:
        updateDefaultTemplateService.updateDefaultTemplateId(guestId, registryId, registryChecklistRequest, RegistryChannel.WEB, RegistrySubChannel.KIOSK).block()

        then:
        1 * checklistTemplateRepository.countByTemplateId(_) >> Mono.error(new RuntimeException())

        thrown(RuntimeException)
    }
}
