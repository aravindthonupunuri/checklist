package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.*
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistrychecklists.test.DataProvider
import com.tgt.backpackregistrychecklists.test.util.RedskyDataProvider
import com.tgt.backpackregistrychecklists.transport.ChecklistResponseTO
import com.tgt.backpackregistryclient.transport.RedskyResponseTO
import com.tgt.backpackregistryclient.transport.RegistryDetailsResponseTO
import com.tgt.backpackregistryclient.transport.RegistryItemsBasicInfoTO
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import javax.inject.Inject
import java.time.LocalDate
import java.time.Month

@MicronautTest
class GetRegistryChecklistsFunctionalTest extends BasePersistenceFunctionalTest{

    Logger LOG = LoggerFactory.getLogger(GetRegistryChecklistsFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository

    @Inject
    RegistryChecklistRepository registryChecklistRepository

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    @Shared
    RegistrySubChannel subChannel = RegistrySubChannel.TGTWEB

    @Shared
    UUID registryId = UUID.randomUUID()

    RedskyDataProvider redskyDataProvider = new RedskyDataProvider()

    def "test get checklist info for a registryId - integrity"() {
        def guestId = "1234"
        def uri = "/registry_checklists/v1/"+registryId+"/checklist_templates?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        def items = [new RegistryItemsBasicInfoTO(registryId, "12954094", null, 2, 0, "itemTitle1", LocalDate.of(2020, Month.DECEMBER, 30), LocalDate.of(2020, Month.DECEMBER, 30)),
                     new RegistryItemsBasicInfoTO(registryId, "22222", null, 2, 0, "itemTitle2", LocalDate.of(2020, Month.APRIL, 12), LocalDate.of(2020, Month.AUGUST, 30)),
                     new RegistryItemsBasicInfoTO(registryId, "55555", null, 2, 0, "itemTitle3", LocalDate.of(2020, Month.DECEMBER, 12), LocalDate.of(2020, Month.DECEMBER, 30)),
                     new RegistryItemsBasicInfoTO(registryId, "44444", null, 2, 0, "itemTitle4", LocalDate.of(2020, Month.MAY, 12), LocalDate.of(2020, Month.OCTOBER, 30)),
                     new RegistryItemsBasicInfoTO(registryId, "33333", null, 2, 0, "itemTitle5", LocalDate.of(2020, Month.MAY, 12), LocalDate.of(2020, Month.SEPTEMBER, 30)),
                     new RegistryItemsBasicInfoTO(registryId, "66666", null, 2, 0, "itemTitle6", null, null)]


        def getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, items, null,
            null, null, null, LocalDate.now())

        RedskyResponseTO redskyResponseTO1 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("12954094", "5xtjw"))
        RedskyResponseTO redskyResponseTO2 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("22222", "5xtjw"))
        RedskyResponseTO redskyResponseTO3 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("55555", "5q0ev"))
        RedskyResponseTO redskyResponseTO4 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("44444", "5xtk4"))
        RedskyResponseTO redskyResponseTO5 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("33333", "54x8u"))
        RedskyResponseTO redskyResponseTO6 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("66666", "5q0eu"))

        def checklistTemplate1 = new ChecklistTemplate(new ChecklistTemplatePK(1, 201), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5xtjw", "5xtjw", "travel system", 1, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())
        def checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK(1, 202), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5xtk7", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "stroller", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())
        def checklistTemplate3 = new ChecklistTemplate(new ChecklistTemplatePK(1, 203), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5q0ev", "5q0ev", "infant car seat", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())
        def checklistTemplate4 = new ChecklistTemplate(new ChecklistTemplatePK(1, 208), RegistryType.BABY,
            "checklistName", true, 1, "29504", "gear &amp; activity", "name",
            "5q0eu", "5q0eu", "baby carrier", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())

        def checkedSubcategories1 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 201), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)
        def checkedSubcategories2 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 202), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)


        registryChecklistRepository.save(new RegistryChecklist(registryId, 1, LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)).block()
        checklistTemplateRepository.save(checklistTemplate1).block()
        checklistTemplateRepository.save(checklistTemplate2).block()
        checklistTemplateRepository.save(checklistTemplate3).block()
        checklistTemplateRepository.save(checklistTemplate4).block()
        registryChecklistSubCategoryRepository.save(checkedSubcategories1).block()
        registryChecklistSubCategoryRepository.save(checkedSubcategories2).block()

        when:
        HttpResponse<ChecklistResponseTO> getChecklistsResponse =
            client.toBlocking().exchange(HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        def actualStatus = getChecklistsResponse.status()
        def result = getChecklistsResponse.body()

        then:
        actualStatus == HttpStatus.OK
        result != null
        result.registryId == registryId
        result.registryItemCount == 6
        result.categories.size() == 2
        result.checklistCheckedCount == 4
        result.checklistTotalCount == 4
        result.categories.findAll( category ->
            category.categoryId == "963002" &&
                category.categoryTotalCount == 3 &&
                category.categoryCheckedCount == 3 &&
                category.subcategories.size() == 3 &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 201 &&
                        subcategory.subcategoryTaxonomyIds == "5xtjw" &&
                        subcategory.itemCount == 2 &&
                        subcategory.lastUpdatedItem.tcin == "12954094"
                ).size() == 1 &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 202 &&
                        subcategory.subcategoryTaxonomyIds == "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u" &&
                        subcategory.itemCount == 2 &&
                        subcategory.lastUpdatedItem.tcin == "44444"
                ).size() == 1 &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 203 &&
                        subcategory.subcategoryTaxonomyIds == "5q0ev" &&
                        subcategory.itemCount == 1 &&
                        subcategory.lastUpdatedItem.tcin == "55555"
                ).size() == 1
        ).size() == 1
        result.categories.findAll( category ->
            category.categoryId == "29504" &&
                category.categoryTotalCount == 1 &&
                category.categoryCheckedCount == 1 &&
                category.subcategories.size() == 1 &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 208 &&
                        subcategory.subcategoryTaxonomyIds == "5q0eu" &&
                        subcategory.itemCount == 1 &&
                        subcategory.lastUpdatedItem.tcin == "66666"
                ).size() == 1
        ).size() == 1

        1 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: getRegistryDetailsResponse]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=12954094") }, _) >> [status: 200, body: redskyResponseTO1]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=22222") }, _) >> [status: 200, body: redskyResponseTO2]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=55555") }, _) >> [status: 200, body: redskyResponseTO3]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=44444") }, _) >> [status: 200, body: redskyResponseTO4]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=33333") }, _) >> [status: 200, body: redskyResponseTO5]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=66666") }, _) >> [status: 200, body: redskyResponseTO6]
    }

    def "test get checklist info - if timestamps from getDetails are null"() {
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def uri = "/registry_checklists/v1/"+registryId+"/checklist_templates?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        def items = [
            new RegistryItemsBasicInfoTO(registryId, "44444", null, 2, 0, "itemTitle4", null, null),
            new RegistryItemsBasicInfoTO(registryId, "33333", null, 2, 0, "itemTitle5", null, null)
        ]

        def getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, items, null,
            null, null, null, LocalDate.now())

        RedskyResponseTO redskyResponseTO4 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("44444", "5xtk4"))
        RedskyResponseTO redskyResponseTO5 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("33333", "54x8u"))

        def checkedSubcategories1 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 201), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)
        def checkedSubcategories2 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 202), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)


        registryChecklistRepository.save(new RegistryChecklist(registryId, 1, LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)).block()
        registryChecklistSubCategoryRepository.save(checkedSubcategories1).block()
        registryChecklistSubCategoryRepository.save(checkedSubcategories2).block()

        when:
        HttpResponse<ChecklistResponseTO> getChecklistsResponse =
            client.toBlocking().exchange(HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        def actualStatus = getChecklistsResponse.status()
        def result = getChecklistsResponse.body()

        then:
        actualStatus == HttpStatus.OK
        result != null
        result.categories.findAll( category ->
            category.categoryId == "963002" &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 202 &&
                    subcategory.lastUpdatedItem.tcin == "44444" &&
                    subcategory.lastUpdatedItem.addedTs == null &&
                        subcategory.lastUpdatedItem.lastModifiedTs == null
                ).size() == 1
        ).size() == 1

        1 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: getRegistryDetailsResponse]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=44444") }, _) >> [status: 200, body: redskyResponseTO4]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=33333") }, _) >> [status: 200, body: redskyResponseTO5]
    }

    def "test get checklist info - if redsky response is null"() {
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def uri = "/registry_checklists/v1/"+registryId+"/checklist_templates?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        def items = [
            new RegistryItemsBasicInfoTO(registryId, "44444", null, 2, 0, "itemTitle4", null, null),
            new RegistryItemsBasicInfoTO(registryId, "33333", null, 2, 0, "itemTitle5", null, null)
        ]

        def getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, items, null,
            null, null, null, LocalDate.now())

        RedskyResponseTO redskyResponseTO4 = new RedskyResponseTO(null, null)
        RedskyResponseTO redskyResponseTO5 = new RedskyResponseTO(null, null)

        def checkedSubcategories1 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 201), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)
        def checkedSubcategories2 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 202), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)


        registryChecklistRepository.save(new RegistryChecklist(registryId, 1, LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)).block()
        registryChecklistSubCategoryRepository.save(checkedSubcategories1).block()
        registryChecklistSubCategoryRepository.save(checkedSubcategories2).block()

        when:
        HttpResponse<ChecklistResponseTO> getChecklistsResponse =
            client.toBlocking().exchange(HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        def actualStatus = getChecklistsResponse.status()
        def result = getChecklistsResponse.body()

        then:
        actualStatus == HttpStatus.OK
        result != null
        result.categories.findAll( category ->
            category.subcategories.findAll( subcategory ->
                subcategory.lastUpdatedItem == null
            )
        ).size() == 2

        1 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: getRegistryDetailsResponse]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=44444") }, _) >> [status: 200, body: redskyResponseTO4]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=33333") }, _) >> [status: 200, body: redskyResponseTO5]

    }

    def "test get checklist info - if there are no items in the registry"() {
        def guestId = "1234"
        def uri = "/registry_checklists/v1/"+registryId+"/checklist_templates?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        RegistryDetailsResponseTO getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", null, [], null,
            null, null, null, LocalDate.now())

        when:
        HttpResponse<ChecklistResponseTO> getChecklistsResponse =
            client.toBlocking().exchange(HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        def actualStatus = getChecklistsResponse.status()
        def result = getChecklistsResponse.body()

        then:
        actualStatus == HttpStatus.OK
        result != null
        result.registryId == registryId
        result.categories.findAll( category ->
            category.categoryId == "963002" &&
            category.subcategories.findAll( subcategory ->
                subcategory.lastUpdatedItem == null &&
                    subcategory.itemCount == 0
            ).size() == 3
        ).size() == 1
        result.categories.findAll( category ->
            category.categoryId == "29504" &&
                category.subcategories.findAll( subcategory ->
                    subcategory.lastUpdatedItem == null &&
                        subcategory.itemCount == 0 &&
                        !subcategory.checked
                ).size() == 1
        ).size() == 1

        1 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: getRegistryDetailsResponse]
        0 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1") }, _) >> [status: 200, body: []]
    }

    def "test get checklist info - if no checklist exists for the given templateId"() {
        def registryId = UUID.randomUUID()
        def guestId = "1234"
        def uri = "/registry_checklists/v1/"+registryId+"/checklist_templates?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        registryChecklistRepository.save(new RegistryChecklist(registryId, 2, LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)).block()

        when:
        client.toBlocking().exchange(HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST

        0 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: []]
        0 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1") }, _) >> [status: 200, body: []]
    }

    def "test get checklist info - if registryId doesn't have an active checklist"() {
        def registryId = UUID.randomUUID()
        def guestId = "1234"
        def uri = "/registry_checklists/v1/"+registryId+"/checklist_templates?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        when:
        client.toBlocking().exchange(HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST

        0 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: []]
        0 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1") }, _) >> [status: 200, body: []]
    }
}
