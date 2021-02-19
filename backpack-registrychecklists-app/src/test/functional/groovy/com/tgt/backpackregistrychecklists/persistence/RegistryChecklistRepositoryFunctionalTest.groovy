package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.LocalDate

@MicronautTest
@Stepwise
class RegistryChecklistRepositoryFunctionalTest extends BasePersistenceFunctionalTest{

    Logger LOG = LoggerFactory.getLogger(RegistryChecklistRepositoryFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    RegistryChecklistRepository registryChecklistRepository

    @Shared
    UUID registryId

    @Shared
    UUID registryId2

    @Shared
    Integer templateId

    @Shared
    String checklistId

    def setupSpec() {
        registryId = UUID.randomUUID()
        registryId2 = UUID.randomUUID()
        templateId = 2
        checklistId = "200"
    }

    def "test save"() {

        LocalDate date = LocalDate.now()
        def registryChecklist = new RegistryChecklist(registryId, templateId, date, "name", date, "null")
        def registryChecklist1 = new RegistryChecklist(UUID.randomUUID(), templateId, date, "name", date, "null")
        def registryChecklist2 = new RegistryChecklist(registryId2, 30, date, "name", date, "null")

        when:
        def actual = registryChecklistRepository.save(registryChecklist).block()
        def actual1 = registryChecklistRepository.save(registryChecklist1).block()
        def actual2 = registryChecklistRepository.save(registryChecklist2).block()

        then:
        actual != null
        actual.registryId == registryId
        actual.templateId == templateId
    }

    def "test delete by templateId"() {

        when:
        def actual = registryChecklistRepository.deleteByTemplateId(2).block()

        then:
        actual != null
        actual == 2
    }

    def "test update"() {

        when:
        def actual = registryChecklistRepository.update(new RegistryChecklist(registryId, 1, LocalDate.now(), RegistrySubChannel.KIOSK.value,
            LocalDate.now(), RegistrySubChannel.KIOSK.value)).block()

        then:
        actual != null
        actual.registryId == registryId
        actual.templateId == 1
    }

    def "test find"() {

        when:
        def actual = registryChecklistRepository.find(registryId2).block()

        then:
        actual != null
        actual.registryId == registryId2
        actual.templateId == 30
    }
}
