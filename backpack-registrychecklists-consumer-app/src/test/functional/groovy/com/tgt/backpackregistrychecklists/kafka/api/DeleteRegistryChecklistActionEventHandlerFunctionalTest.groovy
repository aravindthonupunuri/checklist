package com.tgt.backpackregistrychecklists.kafka.api

import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategories
import com.tgt.backpackregistrychecklists.domain.model.CheckedSubCategoriesId
import com.tgt.backpackregistrychecklists.domain.model.RegistryChecklist
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.service.async.ChecklistService
import com.tgt.backpackregistrychecklists.test.BaseKafkaFunctionalTest
import com.tgt.backpackregistrychecklists.test.PreDispatchLambda
import com.tgt.backpackregistrychecklists.transport.kafka.model.DeleteChecklistActionEvent
import com.tgt.lists.msgbus.ListsMessageBusProducer
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventLifecycleNotificationProvider
import io.micronaut.test.annotation.MicronautTest
import io.opentracing.Tracer
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

import javax.inject.Inject
import java.time.LocalDate
import java.util.stream.Collectors

@MicronautTest
@Stepwise
class DeleteRegistryChecklistActionEventHandlerFunctionalTest extends BaseKafkaFunctionalTest {

    static Logger LOG = LoggerFactory.getLogger(DeleteRegistryChecklistActionEventHandlerFunctionalTest)

    PollingConditions conditions = new PollingConditions(timeout: 30, delay: 1)

    @Shared
    @Inject
    Tracer tracer

    @Shared
    @Inject
    EventLifecycleNotificationProvider eventNotificationsProvider

    @Shared
    TestEventListener testEventListener

    @Inject
    ListsMessageBusProducer listsMessageBusProducer

    @Inject
    RegistryChecklistRepository registryChecklistRepository

    @Inject
    CheckedSubCategoriesRepository checkedSubCategoriesRepository

    @Override
    Logger getLogger() {
        return LOG
    }

    def setupSpec() {
        testEventListener = new TestEventListener(getLogger())
        testEventListener.tracer = tracer
        eventNotificationsProvider.registerListener(testEventListener)
    }

    def setup() {
        testEventListener.reset()
    }

    def "test DeleteRegistryChecklistActionEvent available with no retry state"() {
        given:
        def templateId = 1
        def guestId = "1234"
        DeleteChecklistActionEvent deleteChecklistActionEvent = new DeleteChecklistActionEvent(guestId, templateId, null)

        RegistryChecklist registryChecklist = new RegistryChecklist(UUID.randomUUID(), 1, LocalDate.now(), "user", LocalDate.now(), "user")
        registryChecklistRepository.save(registryChecklist).block()
        CheckedSubCategoriesId checkedSubCategoriesId = new CheckedSubCategoriesId(UUID.randomUUID(), 1, 201)

        CheckedSubCategories checkedSubCategories = new CheckedSubCategories(checkedSubCategoriesId, LocalDate.now(), "user", LocalDate.now(), "user")
        checkedSubCategoriesRepository.save(checkedSubCategories).block()

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == DeleteChecklistActionEvent.getEventType()) {
                    def deleteTemplateId = DeleteChecklistActionEvent.deserialize(data)
                    if (deleteTemplateId.templateId.toString() == deleteChecklistActionEvent.templateId.toString() &&
                        deleteTemplateId.guestId == deleteChecklistActionEvent.guestId) {
                        return true
                    }
                }
                return false
            }
        }
        when:
        listsMessageBusProducer.sendMessage(deleteChecklistActionEvent.getEventType(), deleteChecklistActionEvent, deleteChecklistActionEvent.guestId).block()

        then:
        testEventListener.verifyEvents { consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }

        and:
        def checklist = registryChecklistRepository.deleteByTemplateId(1).block()
        def checkSubCategory = checkedSubCategoriesRepository.deleteByTemplateId(1).block()
        checklist == 0
        checkSubCategory == 0
    }

    def "test DeleteRegistryChecklistActionEvent available with retry state incomplete"() {
        given:
        def templateId = 1
        def guestId = "1234"
        def retryState = ChecklistService.RetryState.serialize(new ChecklistService.RetryState(false, false))
        DeleteChecklistActionEvent deleteChecklistActionEvent = new DeleteChecklistActionEvent(guestId, templateId, retryState)

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == DeleteChecklistActionEvent.getEventType()) {
                    def deleteTemplateId = DeleteChecklistActionEvent.deserialize(data)
                    if (deleteTemplateId.templateId.toString() == deleteChecklistActionEvent.templateId.toString() &&
                        deleteTemplateId.guestId == deleteChecklistActionEvent.guestId) {
                        return true
                    }
                }
                return false
            }
        }
        when:
        listsMessageBusProducer.sendMessage(deleteChecklistActionEvent.getEventType(), deleteChecklistActionEvent, deleteChecklistActionEvent.guestId).block()

        then:
        testEventListener.verifyEvents { consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }

        and:
        def checklist = registryChecklistRepository.deleteByTemplateId(1).block()
        def checkSubCategory = checkedSubCategoriesRepository.deleteByTemplateId(1).block()
        checklist == 0
        checkSubCategory == 0
    }

    def "test DeleteRegistryChecklistActionEvent available with retry state partially complete - checklistRepository delete failed"() {
        given:
        def templateId = 1
        def guestId = "1234"
        def retryState = ChecklistService.RetryState.serialize(new ChecklistService.RetryState(false, true))
        DeleteChecklistActionEvent deleteChecklistActionEvent = new DeleteChecklistActionEvent(guestId, templateId, retryState)

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == DeleteChecklistActionEvent.getEventType()) {
                    def deleteTemplateId = DeleteChecklistActionEvent.deserialize(data)
                    if (deleteTemplateId.templateId.toString() == deleteChecklistActionEvent.templateId.toString() &&
                        deleteTemplateId.guestId == deleteChecklistActionEvent.guestId) {
                        return true
                    }
                }
                return false
            }
        }
        when:
        listsMessageBusProducer.sendMessage(deleteChecklistActionEvent.getEventType(), deleteChecklistActionEvent, deleteChecklistActionEvent.guestId).block()

        then:
        testEventListener.verifyEvents { consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }

        and:
        def checklist = registryChecklistRepository.deleteByTemplateId(1).block()
        def checkSubCategory = checkedSubCategoriesRepository.deleteByTemplateId(1).block()
        checklist == 0
        checkSubCategory == 0
    }

    def "test DeleteRegistryChecklistActionEvent available with retry state partially complete - checkedSubCategoriesRepository delete failed"() {
        given:
        def templateId = 1
        def guestId = "1234"
        def retryState = ChecklistService.RetryState.serialize(new ChecklistService.RetryState(true, false))
        DeleteChecklistActionEvent deleteChecklistActionEvent = new DeleteChecklistActionEvent(guestId, templateId, retryState)

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == DeleteChecklistActionEvent.getEventType()) {
                    def deleteTemplateId = DeleteChecklistActionEvent.deserialize(data)
                    if (deleteTemplateId.templateId.toString() == deleteChecklistActionEvent.templateId.toString() &&
                        deleteTemplateId.guestId == deleteChecklistActionEvent.guestId) {
                        return true
                    }
                }
                return false
            }
        }
        when:
        listsMessageBusProducer.sendMessage(deleteChecklistActionEvent.getEventType(), deleteChecklistActionEvent, deleteChecklistActionEvent.guestId).block()

        then:
        testEventListener.verifyEvents { consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }

        and:
        def checklist = registryChecklistRepository.deleteByTemplateId(1).block()
        def checkSubCategory = checkedSubCategoriesRepository.deleteByTemplateId(1).block()
        checklist == 0
        checkSubCategory == 0
    }
}
