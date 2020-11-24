package com.tgt.backpackregistrychecklists.persistence

import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import io.micronaut.test.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.LocalDateTime

@MicronautTest
@Stepwise
class RegistryChecklistRepositoryFunctionalTest extends BasePersistenceFunctionalTest {

    Logger LOG = LoggerFactory.getLogger(CheckedSubCategoriesRepositoryFunctionalTest)

    @Override
    Logger getLogger() {
        return LOG
    }

    @Inject
    RegistryChecklistRepository registryChecklistRepository

    @Shared
    UUID registryId

    @Shared
    Integer templateId

    @Shared
    String checklistId

    def setupSpec() {
        registryId = UUID.randomUUID()
        templateId = 2
        checklistId = "200"
    }

    def "test save"() {

        LocalDateTime date = LocalDateTime.now()
        def registryChecklist = new RegistryChecklist(registryId, templateId, date, "name", date, "null")
        def registryChecklist1 = new RegistryChecklist(UUID.randomUUID(), templateId, date, "name", date, "null")

        when:
        def actual = registryChecklistRepository.save(registryChecklist).block()
        def actual1 = registryChecklistRepository.save(registryChecklist1).block()

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
}
