package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
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
import com.tgt.backpackregistryclient.util.RegistrySearchVisibility
import com.tgt.backpackregistryclient.util.RegistryStatus
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared

import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

@MicronautTest
class UpdateDefaultTemplateFunctionalTest extends BasePersistenceFunctionalTest {

    Logger LOG = LoggerFactory.getLogger(UpdateDefaultTemplateFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    RegistryChecklistRepository registryChecklistRepository

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    @Inject
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository

    @Shared
    RegistrySubChannel subChannel = RegistrySubChannel.TGTWEB

    @Shared
    UUID registryId = UUID.randomUUID()

    RedskyDataProvider redskyDataProvider = new RedskyDataProvider()

    def "test update default template - integrity"() {
        def guestId = "1234"
        def templateId = 2
        def uri = "/registries_checklists/v1/"+registryId+"/checklist_templates/"+templateId+"?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        def checklistTemplate1 = new ChecklistTemplate(new ChecklistTemplatePK(2, 201), RegistryType.BABY,
            "firstChecklistName", true, 1, "963002", "strollers and car seats", "name",
            "5xtjw", "5xtjw", "travel system", 1, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())

        def items = [new RegistryItemsBasicInfoTO(registryId, "12954094", null, 2, 0, "itemTitle1", LocalDateTime.of(2020, Month.DECEMBER, 30, 0, 0 ,0), LocalDateTime.of(2020, Month.DECEMBER, 30, 0, 0 ,0))]
        def getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", "", null, items, null,
            null, null, null, LocalDate.now(), RegistrySearchVisibility.PUBLIC, RegistryType.BABY, RegistryStatus.@ACTIVE)

        RedskyResponseTO redskyResponseTO1 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("12954094", "5xtjw"))
        def checkedSubcategories1 = new CheckedSubCategories(new CheckedSubCategoriesId(registryId, 1, 201), LocalDate.now(), subChannel.value, LocalDate.now(), subChannel.value)

        checklistTemplateRepository.save(checklistTemplate1).block()
        registryChecklistSubCategoryRepository.save(checkedSubcategories1).block()
        registryChecklistRepository.save(new RegistryChecklist(registryId, 1, LocalDate.now(), RegistrySubChannel.KIOSK.value, LocalDate.now(), RegistrySubChannel.KIOSK.value)).block()
        when:
        HttpResponse<ChecklistResponseTO> updateDefaultTemplateResponse =
            client.toBlocking().exchange(HttpRequest.PUT(uri, "").headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        def actualStatus = updateDefaultTemplateResponse.status()
        def result = updateDefaultTemplateResponse.body()
        def result2 = registryChecklistRepository.find(registryId).block()
        then:
        actualStatus == HttpStatus.OK
        result2 != null
        result2.templateId == templateId
        result != null
        result.registryId == registryId
        result.registryItemCount == 1
        result.categories.size() == 1
        result.categories.findAll( category ->
            category.categoryId == "963002" &&
                category.subcategories.size() == 1 &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 201 &&
                        subcategory.subcategoryTaxonomyIds == "5xtjw" &&
                        subcategory.itemCount == 1 &&
                        subcategory.lastUpdatedItem.tcin == "12954094"
                ).size() == 1
        ).size() == 1

        1 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: getRegistryDetailsResponse]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=12954094") }, _) >> [status: 200, body: redskyResponseTO1]
    }

    def "test update default template - multiple categories"() {
        def guestId = "1234"
        def templateId = 3
        def uri = "/registries_checklists/v1/"+registryId+"/checklist_templates/"+templateId+"?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        def checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK(3, 203), RegistryType.BABY,
            "firstChecklistName", true, 1, "963003", "strollers and car seats", "name",
            "5q0ev", "5q0ev", "infant car seat", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())


        def items = [new RegistryItemsBasicInfoTO(registryId, "12954094", null, 2, 0, "itemTitle1", LocalDateTime.of(2020, Month.DECEMBER, 30, 0, 0 ,0), LocalDateTime.of(2020, Month.DECEMBER, 30, 0, 0 ,0))]
        def getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", "", null, items, null,
            null, null, null, LocalDate.now(), RegistrySearchVisibility.PUBLIC, RegistryType.BABY, RegistryStatus.@ACTIVE)

        RedskyResponseTO redskyResponseTO1 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("12954094", "5xtjw"))

        checklistTemplateRepository.save(checklistTemplate2).block()
        when:
        HttpResponse<ChecklistResponseTO> updateDefaultTemplateResponse =
            client.toBlocking().exchange(HttpRequest.PUT(uri, "").headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        def actualStatus = updateDefaultTemplateResponse.status()
        def result = updateDefaultTemplateResponse.body()
        def result2 = registryChecklistRepository.find(registryId).block()
        then:
        actualStatus == HttpStatus.OK
        result2 != null
        result2.templateId == templateId
        result != null
        result.registryId == registryId
        result.registryItemCount == 1
        result.categories.size() == 1
        result.categories.findAll( category ->
            category.categoryId == "963003" &&
                category.subcategories.size() == 1 &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 203 &&
                        subcategory.subcategoryTaxonomyIds == "5q0ev" &&
                        subcategory.itemCount == 0 &&
                        subcategory.lastUpdatedItem == null
                ).size() == 1
        ).size() == 1

        1 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: getRegistryDetailsResponse]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=12954094") }, _) >> [status: 200, body: redskyResponseTO1]
    }

    def "test update default template - multiple subcategory childIds"() {
        def guestId = "1234"
        def templateId = 4
        def uri = "/registries_checklists/v1/"+registryId+"/checklist_templates/"+templateId+"?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        def checklistTemplate3 = new ChecklistTemplate(new ChecklistTemplatePK(4, 204), RegistryType.BABY,
            "firstChecklistName", true, 1, "963003", "strollers and car seats", "name",
            "5q0ev", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "infant car seat", 2, "name", "reg_type=baby", LocalDate.now(), LocalDate.now())


        def items = [new RegistryItemsBasicInfoTO(registryId, "12954094", null, 2, 0, "itemTitle1", LocalDateTime.of(2020, Month.DECEMBER, 30, 0, 0 ,0), LocalDateTime.of(2020, Month.DECEMBER, 30, 0, 0 ,0))]
        def getRegistryDetailsResponse = new RegistryDetailsResponseTO(registryId, "", "", "", null, items, null,
            null, null, null, LocalDate.now(), RegistrySearchVisibility.PUBLIC, RegistryType.BABY, RegistryStatus.@ACTIVE)

        RedskyResponseTO redskyResponseTO1 = new RedskyResponseTO(null, redskyDataProvider.getChecklistItemDetails("12954094", "5xtk6"))

        checklistTemplateRepository.save(checklistTemplate3).block()
        when:
        HttpResponse<ChecklistResponseTO> updateDefaultTemplateResponse =
            client.toBlocking().exchange(HttpRequest.PUT(uri, "").headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        def actualStatus = updateDefaultTemplateResponse.status()
        def result = updateDefaultTemplateResponse.body()
        def result2 = registryChecklistRepository.find(registryId).block()
        then:
        actualStatus == HttpStatus.OK
        result2 != null
        result2.templateId == templateId
        result != null
        result.registryId == registryId
        result.registryItemCount == 1
        result.categories.size() == 1
        result.categories.findAll( category ->
            category.categoryId == "963003" &&
                category.subcategories.size() == 1 &&
                category.subcategories.findAll( subcategory ->
                    subcategory.checklistId == 204 &&
                        subcategory.subcategoryTaxonomyIds == "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u" &&
                        subcategory.itemCount == 1 &&
                        subcategory.lastUpdatedItem.tcin == "12954094"
                ).size() == 1
        ).size() == 1

        1 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: getRegistryDetailsResponse]
        1 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=12954094") }, _) >> [status: 200, body: redskyResponseTO1]
    }

    def "test update default template - no checklist exists for the given templateId"() {
        def guestId = "1234"
        def templateId = 33
        def uri = "/registries_checklists/v1/"+registryId+"/checklist_templates/"+templateId+"?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        when:
        client.toBlocking().exchange(HttpRequest.PUT(uri, "").headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST

        0 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: []]
        0 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=12954094") }, _) >> [status: 200, body: []]
    }

    def "test update default template - if registryId doesn't have an active checklist"() {
        def guestId = "1234"
        def templateId = 4
        def registryId = UUID.randomUUID()
        def uri = "/registries_checklists/v1/"+registryId+"/checklist_templates/"+templateId+"?channel=WEB&sub_channel=TGTWEB"
        def getRegistryDetailsUri = "/registries/v2/"+registryId+"/summary_details"

        when:
        client.toBlocking().exchange(HttpRequest.PUT(uri, "").headers(DataProvider.getHeaders(guestId)), ChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST

        0 * mockServer.get({ path -> path.contains(getRegistryDetailsUri)},*_) >> [status: 200, body: []]
        0 * mockServer.get({ path -> path.contains("/redsky_aggregations/v1/registry_services/get_registry_checklist_v1?tcin=12954094") }, _) >> [status: 200, body: []]
    }
}
