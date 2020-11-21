package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistrychecklists.test.DataProvider
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistRequestTO
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.time.LocalDateTime

@MicronautTest
class MarkChecklistFunctionalTest extends BasePersistenceFunctionalTest {

    Logger LOG = LoggerFactory.getLogger(MarkChecklistFunctionalTest)

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


    def "test markChecklistId - happy path"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def uri = "/registry_checklists/v1/"+registryId+"/checklists/"+checklistId+"?template_id="+templateId+"&channel=WEB&sub_channel=KIOSK"

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, LocalDateTime.now(), RegistrySubChannel.KIOSK.value,
            LocalDateTime.now(), RegistrySubChannel.KIOSK.value)

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, templateId, 1),
            "firstChecklistName", true, checklistId, "name", "name", "name",
            "1", "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

        registryChecklistRepository.save(registryChecklist).block()
        checklistTemplateRepository.save(checklistTemplate).block()

        when:
        HttpResponse<RegistryChecklistResponseTO> markChecklistIdResponse =
            client.toBlocking().exchange(
                HttpRequest.POST(uri, registryChecklistRequest).headers(DataProvider.getHeaders(guestId)), RegistryChecklistResponseTO)
        def actualStatus = markChecklistIdResponse.status()
        def actual = markChecklistIdResponse.body()

        then:
        actualStatus == HttpStatus.CREATED
        actual.registryId == registryId
        actual.checklistId == checklistId
        actual.checked
    }

    def "test markChecklistId - No templateId found for given registryId"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def uri = "/registry_checklists/v1/"+registryId+"/checklists/"+checklistId+"?template_id="+templateId+"&channel=WEB&sub_channel=KIOSK"

        when:
        client.toBlocking().exchange(HttpRequest.POST(uri, registryChecklistRequest).headers(DataProvider.getHeaders(guestId)), RegistryChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }

    def "test markChecklistId - No checklistId found for given templateId"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 3

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def uri = "/registry_checklists/v1/"+registryId+"/checklists/"+checklistId+"?template_id="+templateId+"&channel=WEB&sub_channel=KIOSK"

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, LocalDateTime.now(), RegistrySubChannel.KIOSK.value,
            LocalDateTime.now(), RegistrySubChannel.KIOSK.value)

        registryChecklistRepository.save(registryChecklist).block()

        when:
        client.toBlocking().exchange(HttpRequest.POST(uri, registryChecklistRequest).headers(DataProvider.getHeaders(guestId)), RegistryChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }


    def "test markChecklistId - Provided RegistryId - TemplateId combination is not valid"() {
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def uri = "/registry_checklists/v1/"+registryId+"/checklists/"+checklistId+"?template_id="+templateId+"&channel=WEB&sub_channel=KIOSK"

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, 4, LocalDateTime.now(), RegistrySubChannel.KIOSK.value,
            LocalDateTime.now(), RegistrySubChannel.KIOSK.value)

        registryChecklistRepository.save(registryChecklist).block()

        when:
        client.toBlocking().exchange(HttpRequest.POST(uri, registryChecklistRequest).headers(DataProvider.getHeaders(guestId)), RegistryChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }

    def "test markChecklistId - Provided TemplateId - ChecklistId combination is not valid"() {
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 1

        RegistryChecklistRequestTO registryChecklistRequest = new RegistryChecklistRequestTO(templateId)
        def uri = "/registry_checklists/v1/"+registryId+"/checklists/"+checklistId+"?template_id="+templateId+"&channel=WEB&sub_channel=KIOSK"

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, LocalDateTime.now(), RegistrySubChannel.KIOSK.value,
            LocalDateTime.now(), RegistrySubChannel.KIOSK.value)

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, templateId, 1),
            "firstChecklistName", true, 202, "name", "name", "name",
            "1", "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

        registryChecklistRepository.save(registryChecklist).block()
        checklistTemplateRepository.save(checklistTemplate).block()

        when:
        client.toBlocking().exchange(HttpRequest.POST(uri, registryChecklistRequest).headers(DataProvider.getHeaders(guestId)), RegistryChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }
}
