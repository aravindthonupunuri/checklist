package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistTemplateResponseTO
import com.tgt.backpackregistrychecklists.test.DataProvider
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.time.LocalDateTime

@MicronautTest
class GetChecklistTemplatesFunctionalTest extends BasePersistenceFunctionalTest{

    Logger LOG = LoggerFactory.getLogger(GetChecklistTemplatesFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    def "Test getAllTemplates - baby - single template"() {
        def guestId = "1234"
        def registryType = "BABY"
        def uri = "/registry_checklists/v1/checklists?registry_type="+registryType+"&channel=WEB&sub_channel=KIOSK"

        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, 1,
            1), "firstChecklistName", true, 1, "name", "name", "name",
            1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())).block()

        when:
        HttpResponse<RegistryChecklistTemplateResponseTO> getAllTemplatesResponse =
            client.toBlocking().exchange(HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), RegistryChecklistTemplateResponseTO)

        def actualStatus = getAllTemplatesResponse.status()
        def actual = getAllTemplatesResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.registryType == RegistryType.BABY
        actual.checklists.size() == 1
        actual.checklists.get(0).templateId == 1
        actual.checklists.get(0).checklistName == "firstChecklistName"
    }

    def "Test getAllTemplates - baby - multiple templates"() {
        def guestId = "1234"
        def registryType = "BABY"
        def uri = "/registry_checklists/v1/checklists?registry_type="+registryType+"&channel=WEB&sub_channel=KIOSK"

        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, 1,
            2), "firstChecklistName", true, 1, "name", "name", "name",
            1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())).block()
        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, 2,
            1), "secondChecklistName", true, 1, "name", "name", "name",
            1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())).block()

        when:
        HttpResponse<RegistryChecklistTemplateResponseTO> getAllTemplatesResponse =
            client.toBlocking().exchange(
                HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), RegistryChecklistTemplateResponseTO)

        def actualStatus = getAllTemplatesResponse.status()
        def actual = getAllTemplatesResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.registryType == RegistryType.BABY
        actual.checklists.size() == 2
        actual.checklists.get(0).templateId == 1
        actual.checklists.get(0).checklistName == "firstChecklistName"
        actual.checklists.get(1).templateId == 2
        actual.checklists.get(1).checklistName == "secondChecklistName"
    }

    def "test getChecklistTemplates - baby - multiple categories but single template"() {
        def guestId = "1234"
        def registryType = "BABY"
        def uri = "/registry_checklists/v1/checklists?registry_type="+registryType+"&channel=WEB&sub_channel=KIOSK"

        ChecklistTemplate checklistTemplate3 = new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.BABY, 1,
            3), "firstChecklistName", true, 1, "name", "name", "name",
            1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

        checklistTemplateRepository.save(checklistTemplate3).block()

        when:
        HttpResponse<RegistryChecklistTemplateResponseTO> getAllTemplatesResponse =
            client.toBlocking().exchange(
                HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), RegistryChecklistTemplateResponseTO)

        def actualStatus = getAllTemplatesResponse.status()
        def actual = getAllTemplatesResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.registryType == RegistryType.BABY
        actual.checklists.size() == 2
        actual.checklists.get(0).checklistName == "firstChecklistName"
        actual.checklists.get(0).templateId == 1
    }

    def "test getChecklistTemplates - no templates for a registry type"() {
        def guestId = "1234"
        def registryType = "WEDDING"
        def uri = "/registry_checklists/v1/checklists?registry_type="+registryType+"&channel=WEB&sub_channel=KIOSK"

        when:
        HttpResponse<RegistryChecklistTemplateResponseTO> getAllTemplatesResponse =
            client.toBlocking().exchange(
                HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), RegistryChecklistTemplateResponseTO)

        def actualStatus = getAllTemplatesResponse.status()
        def actual = getAllTemplatesResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.registryType == RegistryType.WEDDING
        actual.checklists == null
    }

    def "test getChecklistTemplates - wedding - multiple templates of different registryTypes"() {
        def guestId = "1234"
        def registryType = "WEDDING"
        def uri = "/registry_checklists/v1/checklists?registry_type="+registryType+"&channel=WEB&sub_channel=KIOSK"

        ChecklistTemplate checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK(RegistryType.WEDDING, 2,
            1), "weddingChecklistName", true, 1, "name", "name", "name",
            1, "name", 1, "name", "name", LocalDateTime.now(), LocalDateTime.now())

        checklistTemplateRepository.save(checklistTemplate2).block()

        when:
        HttpResponse<RegistryChecklistTemplateResponseTO> getAllTemplatesResponse =
            client.toBlocking().exchange(
                HttpRequest.GET(uri).headers(DataProvider.getHeaders(guestId)), RegistryChecklistTemplateResponseTO)

        def actualStatus = getAllTemplatesResponse.status()
        def actual = getAllTemplatesResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.registryType == RegistryType.WEDDING
        actual.checklists.size() == 1
        actual.checklists.get(0).checklistName == "weddingChecklistName"
        actual.checklists.get(0).templateId == 2
    }
}