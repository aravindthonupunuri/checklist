package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistrychecklists.transport.DefaultChecklistResponseTO
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import java.time.LocalDate

@MicronautTest
class GetDefaultChecklistTemplatesFunctionalTest extends BasePersistenceFunctionalTest{

    Logger LOG = LoggerFactory.getLogger(GetDefaultChecklistTemplatesFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    def "Test getDefaultTemplates - baby"() {
        def registryType = "BABY"
        def uri = "/registries_checklists/v1/" + "/checklist_templates/" + registryType + "?&channel=WEB&sub_channel=TGTWEB"

        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK( 1,
            101), RegistryType.BABY, "firstChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subCategoryChildIds", 1, "name", "taxonomyUrl", "name", LocalDate.now(), LocalDate.now())).block()

        when:
        HttpResponse<DefaultChecklistResponseTO> getDefaultTemplatesResponse =
            client.toBlocking().exchange(HttpRequest.GET(uri), DefaultChecklistResponseTO)

        def actualStatus = getDefaultTemplatesResponse.status()
        def actual = getDefaultTemplatesResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.registryType == RegistryType.BABY
    }

    def "Test getDefaultTemplates - for invalid registry type"() {
        def registryType = "CUSTOM"
        def uri = "/registries_checklists/v1/" + "/checklist_templates/" + registryType + "?&channel=WEB&sub_channel=TGTWEB"

        checklistTemplateRepository.save(new ChecklistTemplate(new ChecklistTemplatePK( 2,
            101), RegistryType.BABY, "firstChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subCategoryChildIds", 1, "name", "taxonomyUrl", "name", LocalDate.now(), LocalDate.now())).block()

        when:
        HttpResponse<DefaultChecklistResponseTO> getDefaultTemplatesResponse =
            client.toBlocking().exchange(HttpRequest.GET(uri), DefaultChecklistResponseTO)

        def actualStatus = getDefaultTemplatesResponse.status()
        def actual = getDefaultTemplatesResponse.body()

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }
}
