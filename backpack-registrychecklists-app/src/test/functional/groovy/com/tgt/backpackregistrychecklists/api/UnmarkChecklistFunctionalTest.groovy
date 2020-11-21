package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistrychecklists.test.DataProvider
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.backpackregistryclient.util.RegistrySubChannel
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
class UnmarkChecklistFunctionalTest extends BasePersistenceFunctionalTest{

    Logger LOG = LoggerFactory.getLogger(UnmarkChecklistFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository

    def "test unMarkChecklistId - happy path"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2

        def uri = "/registry_checklists/v1/"+registryId+"/checklists/"+checklistId+"?template_id="+templateId+"&channel=WEB&sub_channel=KIOSK"

        registryChecklistSubCategoryRepository.save(new CheckedSubCategories(new CheckedSubCategoriesId(registryId, templateId, checklistId),
            LocalDateTime.now(), RegistrySubChannel.KIOSK.value, LocalDateTime.now(), RegistrySubChannel.KIOSK.value)).block()
        when:
        HttpResponse<RegistryChecklistResponseTO> markChecklistIdResponse =
            client.toBlocking().exchange(
                HttpRequest.DELETE(uri).headers(DataProvider.getHeaders(guestId)), RegistryChecklistResponseTO)
        def actualStatus = markChecklistIdResponse.status()
        def actual = markChecklistIdResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.registryId == registryId
        actual.checklistId == checklistId
        actual.templateId == templateId
        !actual.checked
    }

    def "test unMarkChecklistId - if checklistId/templateId doesn't exist"() {
        given:
        def guestId = "1234"
        def registryId = UUID.randomUUID()
        def checklistId = 201
        def templateId = 2

        def uri = "/registry_checklists/v1/"+registryId+"/checklists/"+checklistId+"?template_id="+templateId+"&channel=WEB&sub_channel=KIOSK"

        when:
        client.toBlocking().exchange(HttpRequest.DELETE(uri).headers(DataProvider.getHeaders(guestId)), RegistryChecklistResponseTO)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }
}

