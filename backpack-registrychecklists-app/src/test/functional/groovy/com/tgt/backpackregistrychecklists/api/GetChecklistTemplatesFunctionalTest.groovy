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
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.time.LocalDate

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
        def uri = "/registries_checklists/v1/checklist_templates?registry_type="+registryType+"&channel=WEB&sub_channel=TGTWEB"

        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK( 1,
            101), RegistryType.BABY, "firstChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subCategoryChildIds", 1, "name", "name", LocalDate.now(), LocalDate.now())).block()

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
        def uri = "/registries_checklists/v1/checklist_templates?registry_type="+registryType+"&channel=WEB&sub_channel=TGTWEB"

        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK( 1,
            111), RegistryType.BABY, "firstChecklistName", true, 2, "name", "name", "name",
            "1", "name", "subCategoryChildIds", 1, "name", "name", LocalDate.now(), LocalDate.now())).block()
        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK( 2,
            112), RegistryType.BABY,"secondChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subCategoryChildIds", 1,  "name", "name", LocalDate.now(), LocalDate.now())).block()

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
        def uri = "/registries_checklists/v1/checklist_templates?registry_type="+registryType+"&channel=WEB&sub_channel=TGTWEB"

        ChecklistTemplate checklistTemplate3 = new ChecklistTemplate(new ChecklistTemplatePK( 1,
            100), RegistryType.BABY, "firstChecklistName", true, 3, "name", "name", "name",
            "1", "name", "subCategoryChildIds", 1, "name", "name", LocalDate.now(), LocalDate.now())

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
        def uri = "/registries_checklists/v1/checklist_templates?registry_type="+registryType+"&channel=WEB&sub_channel=TGTWEB"

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
        def uri = "/registries_checklists/v1/checklist_templates?registry_type="+registryType+"&channel=WEB&sub_channel=TGTWEB"

        ChecklistTemplate checklistTemplate2 = new ChecklistTemplate(new ChecklistTemplatePK( 2,
            100), RegistryType.WEDDING, "weddingChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subCategoryChildIds", 1, "name", "name", LocalDate.now(), LocalDate.now())

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
