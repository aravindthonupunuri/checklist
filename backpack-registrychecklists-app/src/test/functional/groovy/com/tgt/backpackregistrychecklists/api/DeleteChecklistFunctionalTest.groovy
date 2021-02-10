package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistrychecklists.test.DataProvider
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.msgbus.ListsMessageBusProducer
import com.tgt.lists.msgbus.event.EventLifecycleNotificationProvider
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import org.slf4j.Logger
import spock.lang.Shared

import javax.inject.Inject
import java.time.LocalDate

@MicronautTest
class DeleteChecklistFunctionalTest extends BasePersistenceFunctionalTest{

    @Shared
    @Inject
    EventLifecycleNotificationProvider eventNotificationsProvider

    @MockBean(ListsMessageBusProducer.class)
    ListsMessageBusProducer createMockListsMessageBusProducer() {
        return newMockMsgbusKafkaProducerClient(eventNotificationsProvider)
    }

    String guestId = "1234"

    @Override
    Logger getLogger() {
        return null
    }

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    def "test delete checklists integrity"() {
        given:
        String uri = "registry_checklists/v1/checklist_templates?template_id=1&channel=web&sub_channel=TGTWEB"

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 401)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.WEDDING, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())

        ChecklistTemplatePK checklistTemplatePK1 = new ChecklistTemplatePK( 2, 401)
        ChecklistTemplate checklistTemplate1 = new ChecklistTemplate(checklistTemplatePK1, RegistryType.WEDDING, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())
        checklistTemplateRepository.save(checklistTemplate).block()
        checklistTemplateRepository.save(checklistTemplate1).block()
        when:
        HttpResponse<Void> response =client.toBlocking()
            .exchange(HttpRequest.DELETE(uri).headers((DataProvider.getHeaders(guestId))), Void)

        def responseStatus = response.status()
        def checklistTemplateResponse1 = checklistTemplateRepository.findByTemplateId(1).collectList().block().size()
        def checklistTemplateResponse2 = checklistTemplateRepository.findByTemplateId(2).collectList().block().size()

        then:
        responseStatus == HttpStatus.NO_CONTENT
        checklistTemplateResponse1 == 0
        checklistTemplateResponse2 == 1
    }

    def "test delete checklists - passing checklist templateId which is not a resource in the checklist template table"() {
        given:
        String uri = "registry_checklists/v1/checklist_templates?template_id=3&channel=web&sub_channel=TGTWEB"

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 401)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())

        ChecklistTemplatePK checklistTemplatePK1 = new ChecklistTemplatePK( 2, 501)
        ChecklistTemplate checklistTemplate1 = new ChecklistTemplate(checklistTemplatePK1, RegistryType.WEDDING, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())
        checklistTemplateRepository.save(checklistTemplate).block()
        checklistTemplateRepository.save(checklistTemplate1).block()
        when:
        HttpResponse<Void> response =client.toBlocking()
            .exchange(HttpRequest.DELETE(uri).headers((DataProvider.getHeaders(guestId))), Void)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }
}
