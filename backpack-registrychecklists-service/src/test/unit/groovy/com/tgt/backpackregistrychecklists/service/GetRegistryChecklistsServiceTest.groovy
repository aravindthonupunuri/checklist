package com.tgt.backpackregistrychecklists.service

import com.tgt.backpack.redsky.components.client.RedSkyClient
import com.tgt.backpack.redsky.components.manager.RedskyHydrationManager
import com.tgt.backpackregistrychecklists.domain.model.*
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.test.util.RedskyDataProvider
import com.tgt.backpackregistryclient.client.BackpackRegistryClient
import com.tgt.backpackregistryclient.transport.RedskyResponseTO
import com.tgt.backpackregistryclient.transport.RegistryDetailsResponseTO
import com.tgt.backpackregistryclient.transport.RegistryItemsTO
import com.tgt.backpackregistryclient.transport.redsky.getchecklistitem.*
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Shared
import spock.lang.Specification
import java.time.LocalDate
import java.time.Month

class GetRegistryChecklistsServiceTest extends Specification{

    GetRegistryChecklistsService getRegistryChecklistsService
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository
    RegistryChecklistRepository registryChecklistRepository
    ChecklistTemplateRepository checklistTemplateRepository
    BackpackRegistryClient backPackRegistryClient
    RedSkyClient redSkyClient
    RedskyHydrationManager redskyHydrationManager

    @Shared
    RegistryChannel channel = RegistryChannel.WEB
    @Shared
    RegistrySubChannel subChannel = RegistrySubChannel.TGTWEB
    @Shared
    UUID registryId = UUID.randomUUID()
    @Shared
    List<RegistryItemsTO> items
    @Shared
    RegistryChecklist registryChecklist
    @Shared
    ChecklistTemplate checklistTemplate1
    @Shared
    ChecklistTemplate checklistTemplate2
    @Shared
    ChecklistTemplate checklistTemplate3
    @Shared
    RegistryDetailsResponseTO getRegistryDetailsResponse
    @Shared
    RedskyResponseTO redskyItemDetails1
    @Shared
    RedskyResponseTO redskyItemDetails2
    @Shared
    RedskyResponseTO redskyItemDetails3
    @Shared
    CheckedSubCategories checkedSubcategories1
    @Shared
    CheckedSubCategories checkedSubcategories2
    @Shared
    CheckedSubCategories checkedSubcategories3
    @Shared
    RedskyDataProvider redskyDataProvider

    def setup() {
        registryChecklistSubCategoryRepository = Mock(CheckedSubCategoriesRepository)
        registryChecklistRepository = Mock(RegistryChecklistRepository)
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        backPackRegistryClient = Mock(BackpackRegistryClient)
        redSkyClient = Mock(RedSkyClient)
        redskyHydrationManager = new RedskyHydrationManager(redSkyClient)
        getRegistryChecklistsService = new GetRegistryChecklistsService(checklistTemplateRepository, registryChecklistRepository, registryChecklistSubCategoryRepository,
            backPackRegistryClient, redskyHydrationManager)
    }

    def setupSpec() {
        redskyDataProvider = new RedskyDataProvider()

        registryChecklist = new RegistryChecklist(registryId, 1, LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)
        checklistTemplate1 = new ChecklistTemplate(new ChecklistTemplatePK(1, 201), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5xtjw", "5xtjw", "travel system", 1, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())
        checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK(1, 202), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5xtk7", "5xtk7", "stroller", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())
        checklistTemplate3 = new ChecklistTemplate(new ChecklistTemplatePK(1, 203), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5q0ev", "5q0ev", "infant car seat", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())

        items = [new RegistryItemsTO(registryId, "12954094", null, 2, 0, "itemTitle1", LocalDate.of(2020, Month.DECEMBER, 30), LocalDate.of(2020, Month.DECEMBER, 30)),
                 new RegistryItemsTO(registryId, "22222", null, 2, 0, "itemTitle2", LocalDate.of(2020, Month.DECEMBER, 12), LocalDate.of(2020, Month.DECEMBER, 30)),
                 new RegistryItemsTO(registryId, "55555", null, 2, 0, "itemTitle3", LocalDate.of(2020, Month.DECEMBER, 12), LocalDate.of(2020, Month.DECEMBER, 30))]
        getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, items, null,
            null, null, null, LocalDate.now())

        redskyItemDetails1 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("12954094", "5xtjw"))
        redskyItemDetails2 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("22222", "5xtjw"))
        redskyItemDetails3 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("55555", "5q0ev"))

        checkedSubcategories1 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 201), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)
        checkedSubcategories2 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 202), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)
        checkedSubcategories3 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 203), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)
    }

    def "test get checklist info for a registryId - integrity"() {
        given:
        def guestId = "1234"

        when:
        def result = getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate1, checklistTemplate2, checklistTemplate3)
        1 * backPackRegistryClient.getRegistryDetails(*_) >> Mono.just(getRegistryDetailsResponse)
        1 * redSkyClient.getRegistryChecklistItem("12954094") >> Mono.just(redskyItemDetails1)
        1 * redSkyClient.getRegistryChecklistItem("22222") >> Mono.just(redskyItemDetails2)
        1 * redSkyClient.getRegistryChecklistItem("55555") >> Mono.just(redskyItemDetails3)
        1 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.just(checkedSubcategories1, checkedSubcategories2)

        result != null
        result.registryId == registryId
        result.registryItemCount == 3
        result.categories.size() == 1
        result.component3().get(0).categoryId == "963002"
        result.component3().get(0).subcategories.size() == 3
        result.component3().get(0).subcategories.get(0).checklistId == 201
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(0).subcategories.get(0).itemCount == 2
        result.component3().get(0).subcategories.get(0).lastUpdatedItem.tcin == "12954094"
        result.component3().get(0).subcategories.get(1).checklistId == 202
        result.component3().get(0).subcategories.get(2).checklistId == 203
        result.component3().get(0).subcategories.get(2).subcategoryChildIds == "5q0ev"
        result.component3().get(0).subcategories.get(2).itemCount == 1
        result.component3().get(0).subcategories.get(2).lastUpdatedItem.tcin == "55555"
    }

    def "test get checklist info - multiple categories"() {
        given:
        def guestId = "1234"

        def checklistTemplate4 = new ChecklistTemplate(new ChecklistTemplatePK(1, 203), RegistryType.BABY,
            "firstChecklistName", true, 1, "963003", "strollers and car seats", "name",
            "5q0ev", "5q0ev", "infant car seat", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())

        when:
        def result = getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * backPackRegistryClient.getRegistryDetails(*_) >> Mono.just(getRegistryDetailsResponse)
        1 * redSkyClient.getRegistryChecklistItem("12954094") >> Mono.just(redskyItemDetails1)
        1 * redSkyClient.getRegistryChecklistItem("22222") >> Mono.just(redskyItemDetails2)
        1 * redSkyClient.getRegistryChecklistItem("55555") >> Mono.just(redskyItemDetails3)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate1, checklistTemplate2, checklistTemplate4)
        1 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.just(checkedSubcategories1)

        result != null
        result.registryId == registryId
        result.registryItemCount == 3
        result.categories.size() == 2
        result.component3().get(0).categoryId == "963003"
        result.component3().get(0).subcategories.size() == 1
        result.component3().get(1).subcategories.size() == 2
        result.component3().get(1).subcategories.get(0).checklistId == 201
        result.component3().get(1).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(1).subcategories.get(0).itemCount == 2
        result.component3().get(1).subcategories.get(0).lastUpdatedItem.tcin == "12954094"
        result.component3().get(1).subcategories.get(1).checklistId == 202
        !result.component3().get(1).subcategories.get(1).checked
        result.component3().get(0).subcategories.get(0).checklistId == 203
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5q0ev"
        result.component3().get(0).subcategories.get(0).itemCount == 1
        result.component3().get(0).subcategories.get(0).lastUpdatedItem.tcin == "55555"
    }

    def "test get checklist info - if subcategoryChildIds is a list"() {
        given:
        def guestId = "1234"

        def checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK(1, 202), RegistryType.BABY,
            "firstChecklistName", true, 1, "963003", "strollers and car seats", "name",
            "5xtk7", "5xtk7, 5q0ev", "stroller", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())

        items.add(new RegistryItemsTO(registryId, "44444", null, 2, 0, "itemTitle4", LocalDate.of(2020, Month.MAY, 30), LocalDate.of(2020, Month.APRIL, 30)))
        RegistryDetailsResponseTO getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, items, null,
            null, null, null, LocalDate.now())
        def redskyItemDetails4 = new RedskyResponseTO(null, new ChecklistItemDetailsVO(new ChecklistItemDetails("44444",new ItemVO(new ProductDescriptionVO("itemTitle4"),
            new Enrichment(new ImageVO("primary.image.url"))), new TaxonomyVO(new Category("5xtk7")))))

        when:
        def result = getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate1, checklistTemplate2)
        1 * backPackRegistryClient.getRegistryDetails(*_) >> Mono.just(getRegistryDetailsResponse)
        1 * redSkyClient.getRegistryChecklistItem("12954094") >> Mono.just(redskyItemDetails1)
        1 * redSkyClient.getRegistryChecklistItem("22222") >> Mono.just(redskyItemDetails2)
        1 * redSkyClient.getRegistryChecklistItem("55555") >> Mono.just(redskyItemDetails3)
        1 * redSkyClient.getRegistryChecklistItem("44444") >> Mono.just(redskyItemDetails4)
        1 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.empty()

        result != null
        result.registryId == registryId
        result.registryItemCount == 4
        result.categories.size() == 2
        result.component3().get(0).categoryId == "963003"
        result.component3().get(0).subcategories.size() == 1
        result.component3().get(1).subcategories.size() == 1
        result.component3().get(1).subcategories.get(0).checklistId == 201
        result.component3().get(1).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(1).subcategories.get(0).itemCount == 2
        result.component3().get(1).subcategories.get(0).lastUpdatedItem.tcin == "12954094"
        result.component3().get(0).subcategories.get(0).checklistId == 202
        result.component3().get(1).subcategories.get(0).checked
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtk7, 5q0ev"
        result.component3().get(0).subcategories.get(0).itemCount == 2
        result.component3().get(0).subcategories.get(0).lastUpdatedItem.tcin == "55555"
    }

    def "test get checklist info - if registry details response is empty"() {
        given:
        def guestId = "1234"
        def checklistTemplate4 = new ChecklistTemplate(new ChecklistTemplatePK(1, 202), RegistryType.BABY,
            "firstChecklistName", true, 1, "963003", "strollers and car seats", "name", "5xtk7",
            "5xtk7, 5q0ev", "stroller", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())

        when:
        def result = getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate1, checklistTemplate4)
        1 * backPackRegistryClient.getRegistryDetails(*_) >> Mono.empty()
        1 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.just(checkedSubcategories1)

        result != null
        result.registryId == registryId
        result.registryItemCount == 0
        result.categories.size() == 2
        result.component3().get(0).categoryId == "963003"
        result.component3().get(0).subcategories.size() == 1
        result.component3().get(1).subcategories.size() == 1
        result.component3().get(1).subcategories.get(0).checklistId == 201
        result.component3().get(1).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(1).subcategories.get(0).itemCount == 0
        result.component3().get(0).subcategories.get(0).checklistId == 202
        result.component3().get(1).subcategories.get(0).checked
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtk7, 5q0ev"
        result.component3().get(0).subcategories.get(0).itemCount == 0
    }

    def "test get checklist info - if there are no items in the registry"() {
        given:
        def guestId = "1234"
        def checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK(1, 202), RegistryType.BABY,
            "firstChecklistName", true, 1, "963003", "strollers and car seats", "name", "5xtk7",
            "5xtk7, 5q0ev", "stroller", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())

        RegistryDetailsResponseTO getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, [], null,
            null, null, null, LocalDate.now())

        when:
        def result = getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate1, checklistTemplate2)
        1 * backPackRegistryClient.getRegistryDetails(*_) >> Mono.just(getRegistryDetailsResponse)
        1 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.just(checkedSubcategories1)

        result != null
        result.registryId == registryId
        result.registryItemCount == 0
        result.categories.size() == 2
        result.component3().get(0).categoryId == "963003"
        result.component3().get(0).subcategories.size() == 1
        result.component3().get(1).subcategories.size() == 1
        result.component3().get(1).subcategories.get(0).checklistId == 201
        result.component3().get(1).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(1).subcategories.get(0).itemCount == 0
        result.component3().get(0).subcategories.get(0).checklistId == 202
        result.component3().get(1).subcategories.get(0).checked
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtk7, 5q0ev"
        result.component3().get(0).subcategories.get(0).itemCount == 0
    }

    def "test get checklist info - if no checklist exists for the given templateId"() {
        given:
        def guestId = "1234"
        when:
        getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.empty()
        0 * backPackRegistryClient.getRegistryDetails(*_) >> Mono.just(new RegistryDetailsResponseTO())
        0 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.just(checkedSubcategories1)

        def error = thrown(BadRequestException)
        error.message.contains("No checklist exists for the given templateId - ")
    }

    def "test get checklist info - if registryId doesn't have an active checklist"() {
        given:
        def guestId = "1234"
        when:
        getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.empty()
        0 * checklistTemplateRepository.findByTemplateId(1) >> Flux.empty()
        0 * backPackRegistryClient.getRegistry(*_) >> Mono.just(new RegistryDetailsResponseTO())
        0 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.just(checkedSubcategories1)

        def error = thrown(BadRequestException)
        error.message.contains("RegistryId-$registryId doesn't have an active checklist")
    }

    def "test get checklist info - if item createdTs or updatedTs is null"() {
        given:
        def guestId = "1234"

        def items = [new RegistryItemsTO(registryId, "12954094", null, 2, 0, "itemTitle1", null, null),
                     new RegistryItemsTO(registryId, "22222", null, 2, 0, "itemTitle2", null, null),
                     new RegistryItemsTO(registryId, "55555", null, 2, 0, "itemTitle3", null, null)]
        RegistryDetailsResponseTO getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, items, null,
            null, null, null, LocalDate.now())

        when:
        def result = getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel).block()

        then:
        1 * registryChecklistRepository.find(registryId) >> Mono.just(registryChecklist)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate1, checklistTemplate2, checklistTemplate3)
        1 * backPackRegistryClient.getRegistryDetails(*_) >> Mono.just(getRegistryDetailsResponse)
        1 * redSkyClient.getRegistryChecklistItem("12954094") >> Mono.just(redskyItemDetails1)
        1 * redSkyClient.getRegistryChecklistItem("22222") >> Mono.just(redskyItemDetails2)
        1 * redSkyClient.getRegistryChecklistItem("55555") >> Mono.just(redskyItemDetails3)
        1 * registryChecklistSubCategoryRepository.findByRegistryIdAndTemplateId(registryId, 1) >> Flux.just(checkedSubcategories1, checkedSubcategories2)

        result != null
        result.registryId == registryId
        result.registryItemCount == 3
        result.categories.size() == 1
        result.component3().get(0).categoryId == "963002"
        result.component3().get(0).subcategories.size() == 3
        result.component3().get(0).subcategories.get(0).checklistId == 201
        result.component3().get(0).subcategories.get(0).subcategoryChildIds == "5xtjw"
        result.component3().get(0).subcategories.get(0).itemCount == 2
        result.component3().get(0).subcategories.get(0).lastUpdatedItem.tcin == "12954094"
        result.component3().get(0).subcategories.get(1).checklistId == 202
        result.component3().get(0).subcategories.get(2).checklistId == 203
        result.component3().get(0).subcategories.get(2).subcategoryChildIds == "5q0ev"
        result.component3().get(0).subcategories.get(2).itemCount == 1
        result.component3().get(0).subcategories.get(2).lastUpdatedItem.tcin == "55555"
    }
}
