package com.tgt.backpackregistrychecklists.kafka.api

import com.tgt.backpackregistrychecklists.domain.model.*
import com.tgt.backpackregistrychecklists.kafka.model.CheckListMarkNotifyEvent
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.test.BaseKafkaFunctionalTest
import com.tgt.backpackregistrychecklists.test.PreDispatchLambda
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.micronaut.persistence.instrumentation.DatabaseExecTestListener
import com.tgt.lists.micronaut.persistence.instrumentation.RepositoryInstrumenter
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
class CheckListMarkNotifyEventHandlerFunctionalTest extends BaseKafkaFunctionalTest {

    static Logger LOG = LoggerFactory.getLogger(CheckListMarkNotifyEventHandlerFunctionalTest)

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
    ChecklistTemplateRepository checklistTemplateRepository

    @Inject
    CheckedSubCategoriesRepository registryChecklistSubCategoryRepository

    @Inject
    RepositoryInstrumenter repositoryInstrumenter

    @Shared
    boolean executeTimeout = false

    def checkListId
    def registryId
    def templateId
    def registrySubChannel
    def registryType

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
        repositoryInstrumenter.attachTestListener(new DatabaseExecTestListener() {
            @Override
            boolean shouldOverrideWithTimeoutQuery(@NotNull String repoName, @NotNull String methodName) {
                return executeTimeout
            }
        })
    }

    @Override
    Map<String, String> getAdditionalProperties() {
        return ["jdbc-stmt.serverStatementTimeoutMillis": "50"]
    }

    def "test checkListMarkNotifyEvent as marked integrity"() {
        given:
        registryId = UUID.randomUUID()
        checkListId = 201
        templateId = 1
        registrySubChannel = RegistrySubChannel.KIOSK
        registryType = RegistryType.BABY

        def checkListMarkNotifyEvent = new CheckListMarkNotifyEvent(registryId, checkListId, templateId, true, registrySubChannel, null)

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, LocalDate.now(), registrySubChannel.value, LocalDate.now(), registrySubChannel.value)

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK( templateId, checkListId), registryType,
            "firstChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subcategory_child_ids", 1, "name", "name", LocalDate.now(), LocalDate.now())

        registryChecklistRepository.save(registryChecklist).block()
        checklistTemplateRepository.save(checklistTemplate).block()

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == CheckListMarkNotifyEvent.getEventType()) {
                    def event = CheckListMarkNotifyEvent.deserialize(data)
                    if (event.listId.toString() == checkListMarkNotifyEvent.listId.toString()) {
                        return true
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(checkListMarkNotifyEvent.getEventType(), checkListMarkNotifyEvent, registryId).block()

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
    }

    def "test checkListMarkNotifyEvent as unmarked integrity"() {
        given:
        registryId = UUID.randomUUID()
        checkListId = 201
        templateId = 1
        registrySubChannel = RegistrySubChannel.KIOSK
        registryType = RegistryType.BABY

        def checkListMarkNotifyEvent = new CheckListMarkNotifyEvent(registryId, checkListId, templateId, false, registrySubChannel, null)

        registryChecklistSubCategoryRepository.save(new CheckedSubCategories(new CheckedSubCategoriesId(registryId, templateId, checkListId),
            LocalDate.now(), registrySubChannel.value, LocalDate.now(), registrySubChannel.value)).block()

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == CheckListMarkNotifyEvent.getEventType()) {
                    def event = CheckListMarkNotifyEvent.deserialize(data)
                    if (event.listId.toString() == checkListMarkNotifyEvent.listId.toString()) {
                        return true
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(checkListMarkNotifyEvent.getEventType(), checkListMarkNotifyEvent, registryId).block()

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
    }

    def "test checkListMarkNotifyEvent failure scenario"() {
        given:
        registryId = UUID.randomUUID()
        checkListId = 201
        templateId = 1
        registrySubChannel = RegistrySubChannel.KIOSK
        registryType = RegistryType.BABY
        executeTimeout = true

        def checkListMarkNotifyEvent = new CheckListMarkNotifyEvent(registryId, checkListId, templateId, true, registrySubChannel, null)

        RegistryChecklist registryChecklist = new RegistryChecklist(registryId, templateId, LocalDate.now(), registrySubChannel.value, LocalDate.now(), registrySubChannel.value)

        ChecklistTemplate checklistTemplate = new ChecklistTemplate(new ChecklistTemplatePK( templateId, checkListId), registryType,
            "firstChecklistName", true, 1, "name", "name", "name",
            "1", "name", "subcategory_child_ids", 1, "name", "name", LocalDate.now(), LocalDate.now())

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == CheckListMarkNotifyEvent.getEventType()) {
                    def event = CheckListMarkNotifyEvent.deserialize(data)
                    if (event.listId.toString() == checkListMarkNotifyEvent.listId.toString()) {
                        if (eventHeaders.source == "backpackregistrychecklists-migration-dlq") {
                            executeTimeout = false
                            registryChecklistRepository.save(registryChecklist).block()
                            checklistTemplateRepository.save(checklistTemplate).block()
                            return true
                        }
                        if (eventHeaders.source == "backpackregistrychecklists-migration") {
                            return true
                        }
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(checkListMarkNotifyEvent.getEventType(), checkListMarkNotifyEvent, registryId).block()

        then:
        testEventListener.verifyEvents { consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch)
                }.collect(Collectors.toList())
                assert completedEvents.any{
                    it.success
                } // after the first time failure , message from dlq will be processed and completed
                assert ((TestEventListener.Result) producerEvents[0]).topic == "registry-internal-data-bus-stage"
                // first message sendMessage when its called
                assert ((TestEventListener.Result) producerEvents[1]).topic == "registry-internal-data-bus-stage-dlq"
                // on failure putting it to dlq
            }
        }
    }
}
