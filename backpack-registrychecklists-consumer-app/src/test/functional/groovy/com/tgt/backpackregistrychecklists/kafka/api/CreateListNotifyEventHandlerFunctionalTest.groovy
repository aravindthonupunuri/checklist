package com.tgt.backpackregistrychecklists.kafka.api

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.test.BaseKafkaFunctionalTest
import com.tgt.backpackregistrychecklists.test.PreDispatchLambda
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.atlas.api.type.LIST_STATE
import com.tgt.lists.atlas.kafka.model.CreateListNotifyEvent
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
class CreateListNotifyEventHandlerFunctionalTest extends BaseKafkaFunctionalTest {

    static Logger LOG = LoggerFactory.getLogger(CreateListNotifyEventHandlerFunctionalTest)

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

    static String guestId
    static UUID registryId
    static String registryTitle
    static String registryType
    static String channel
    static String subChannel

    @Override
    Logger getLogger() {
        return LOG
    }

    def setupSpec() {
        testEventListener = new TestEventListener(getLogger())
        testEventListener.tracer = tracer
        eventNotificationsProvider.registerListener(testEventListener)
        registryTitle = "List title 1"
        registryType = RegistryType.BABY.name()
        channel = RegistryChannel.WEB.name()
        subChannel =  RegistrySubChannel.TGTWEB.name()
    }

    def setup() {
        testEventListener.reset()
    }

    def "test createListNotifyEventHandler integrity"() {
        given:
        registryId = UUID.randomUUID()
        guestId = UUID.randomUUID().toString()

        def createListNotifyEvent = new CreateListNotifyEvent(guestId, registryId,
            listType, registryType, registryTitle, channel, subChannel, LIST_STATE.INACTIVE, null,
            LocalDate.now(), null, null, null, null, null)

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "checklistName", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())


        checklistTemplateRepository.save(checklistTemplate).block()
        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == CreateListNotifyEvent.getEventType()) {
                    def createRegistry = CreateListNotifyEvent.deserialize(data)
                    if (createRegistry.listId.toString() == createListNotifyEvent.listId.toString() &&
                        createRegistry.guestId == createListNotifyEvent.guestId) {
                        return true
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(createListNotifyEvent.getEventType(), createListNotifyEvent, guestId).block()

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

    def "test createListNotifyEventHandler - when default templateId is not found in checklist template table"() {
        given:
        registryId = UUID.randomUUID()
        guestId = UUID.randomUUID().toString()

        def createListNotifyEvent = new CreateListNotifyEvent(guestId, registryId, listType, registryType,
            registryTitle, channel, subChannel, LIST_STATE.INACTIVE, null, LocalDate.now(), null,
        null, null, null, null)

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 3)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "checklistName", false, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "5xtk4,5xtk3,5xtk2,5xtk5,5xtk6,54x8u", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDate.now(), LocalDate.now())


        checklistTemplateRepository.save(checklistTemplate).block()
        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == CreateListNotifyEvent.getEventType()) {
                    def createRegistry = CreateListNotifyEvent.deserialize(data)
                    if (createRegistry.listId.toString() == createListNotifyEvent.listId.toString() &&
                        createRegistry.guestId == createListNotifyEvent.guestId) {
                        return true
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(createListNotifyEvent.getEventType(), createListNotifyEvent, guestId).block()

        then:
        testEventListener.verifyEvents { consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 0
            }
        }
    }
}
