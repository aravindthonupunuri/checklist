package com.tgt.backpackregistrychecklists.test

import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingLifecycleListener
import io.micronaut.context.annotation.Value
import io.micronaut.test.support.TestPropertyProvider
import io.opentracing.Span
import io.opentracing.Tracer
import org.apache.kafka.clients.admin.AdminClient
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.KafkaContainer
import spock.lang.Shared

import javax.inject.Inject
import java.util.concurrent.ConcurrentLinkedQueue

abstract class BaseKafkaFunctionalTest extends BasePersistenceFunctionalTest implements TestPropertyProvider {

    static Logger baseLogger = LoggerFactory.getLogger(BaseKafkaFunctionalTest)

    @Shared
    static KafkaContainer kafkaContainer

    @Shared
    @Inject
    AdminClient adminClient

    @Value("\${msgbus.kafka.consumer-group}")
    String msgbusConsumerGroup

    @Value("\${msgbus.kafka.dlq-consumer-group}")
    String dlqConsumerGroup

    @Override
    Map<String, String> getProperties() {
        def map = Object.getProperties()
        String kafkaBootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS")

        if (kafkaBootstrapServers == null) {
            baseLogger.info("Using testcontainer kafka")

            if (kafkaContainer == null) {
                baseLogger.info("starting testcontainer kafka")
                // kafka default for auto.create.topics.enable is "true" which means topics will be auto created
                // when a producer tries to produce to a topic that doesn't exists yet.
                kafkaContainer = new KafkaContainer("4.1.2")
                kafkaContainer.start()
            }

            kafkaBootstrapServers = kafkaContainer.getBootstrapServers()
            baseLogger.info("getProperties [kafka.bootstrap.servers: $kafkaBootstrapServers]")
            map.put("kafka.bootstrap.servers", "${kafkaBootstrapServers}")
        }
        else {
            // use drone's kafka service
            baseLogger.info("using drone kafka service $kafkaBootstrapServers")
            map.put("kafka.bootstrap.servers", kafkaBootstrapServers)
        }
        return map
    }

    static class TestEventListener implements EventProcessingLifecycleListener {
        private PreDispatchLambda preDispatchLambda = null
        PostCompletionLambda postCompletionLambda = null
        Tracer tracer = null

        Logger testLogger

        TestEventListener(Logger testLogger) {
            this.testLogger = testLogger
        }

        class Result {
            public String topic
            public boolean success
            public EventHeaders eventHeaders
            public Object data
            public Span activeSpan
            public boolean preDispatch
            public boolean isPoisonEvent

            Result(String topic, boolean success, EventHeaders eventHeaders, Object result, Span activeSpan, boolean preDispatch, boolean isPoisonEvent) {
                this.topic = topic
                this.success = success
                this.eventHeaders = eventHeaders
                this.data = result
                this.activeSpan = activeSpan
                this.preDispatch = preDispatch
                this.isPoisonEvent = isPoisonEvent
            }
        }

        class ConsumerStatus {
            String consumerName
            boolean paused

            ConsumerStatus(String consumerName, boolean paused) {
                this.consumerName = consumerName
                this.paused = paused
            }
        }

        private ConcurrentLinkedQueue<Result> consumerEvents = new ConcurrentLinkedQueue<>()
        private ConcurrentLinkedQueue<Result> producerEvents = new ConcurrentLinkedQueue<>()
        private ConcurrentLinkedQueue<ConsumerStatus> consumerStatusEvents = new ConcurrentLinkedQueue<>()

        void reset() {
            consumerEvents.clear()
            producerEvents.clear()
            consumerStatusEvents.clear()
            preDispatchLambda = null
            postCompletionLambda = null
        }

        void verifyEvents(Closure closure) {
            try {
                closure(consumerEvents, producerEvents, consumerStatusEvents)
            }
            catch(Throwable t) {
                int idx = 1
                String events = ""
                consumerEvents.forEach {
                    EventHeaders headers = it.eventHeaders
                    events += "\nevent[${idx++}]: ${headers} [ success: $it.success, preDispatch: $it.preDispatch], activeSpan: ${it.activeSpan != null}"
                }
                testLogger.error("Test ConsumerEvents: $events")

                events = ""
                producerEvents.eventHeaders.forEach {
                    events += "\nevent[${idx++}]: ${it}"
                }
                testLogger.error("Test ProducerEvents: $events")
                throw t
            }
        }

        @Override
        boolean onPreDispatchConsumerEvent(@NotNull String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
            testLogger.info("Received onPreDispatch(topic: $topic): "+eventHeaders)
            consumerEvents.add(new Result(topic, false, eventHeaders, data, tracer.activeSpan(), true, isPoisonEvent))
            if (preDispatchLambda)
                return preDispatchLambda.onPreDispatchConsumerEvent(topic, eventHeaders, data, isPoisonEvent)
            else {
                testLogger.info("Missing preDispatchLambda: discarding event (topic: $topic): "+eventHeaders)
            }
            return false
        }

        @Override
        void onPostCompletionConsumerEvent(@NotNull String topic, boolean success, @NotNull EventHeaders eventHeaders, @Nullable Object result, boolean isPoisonEvent, @Nullable Throwable error) {
            testLogger.info("Received onPostCompletion(topic: $topic): "+eventHeaders)
            consumerEvents.add(new Result(topic, success, eventHeaders, result, tracer.activeSpan(), false, isPoisonEvent))
            if (postCompletionLambda)
                postCompletionLambda.onPostCompletionConsumerEvent(topic, success, eventHeaders, result, isPoisonEvent, error)
        }

        @Override
        void onConsumerDeadEventPreCompletion(@NotNull String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data) {
            testLogger.info("Received onConsumerDeadEventPreCompletion(topic: $topic): "+eventHeaders)
            consumerEvents.add(new Result(topic, false, eventHeaders, null, tracer.activeSpan(), true, false))
        }

        @Override
        void onConsumerDeadEventPostCompletion(@NotNull String topic, boolean success, @NotNull EventHeaders eventHeaders, @Nullable Throwable error) {
            testLogger.info("Received onConsumerDeadEventPostCompletion(topic: $topic): "+eventHeaders)
            consumerEvents.add(new Result(topic, success, eventHeaders, null, tracer.activeSpan(), false, false))
        }

        @Override
        void onSuccessfulProducerSendEvent(@NotNull String topic, @NotNull EventHeaders eventHeaders, @NotNull Object message, @NotNull Object partitionKey) {
            testLogger.info("Received onSuccessfulProducerSendEvent(topic: $topic): "+eventHeaders)
            producerEvents.add(new Result(topic, true, eventHeaders, message, tracer.activeSpan(), false, false))
        }

        @Override
        void onFailedProducerSendEvent(@NotNull String topic, @NotNull EventHeaders eventHeaders, @NotNull Object message, @NotNull Object partitionKey) {
            testLogger.info("Received onFailedProducerSendEvent(topic: $topic): "+eventHeaders)
            producerEvents.add(new Result(topic, false, eventHeaders, message, tracer.activeSpan(), false, false))
        }

        @Override
        void onConsumerPause(@NotNull String consumerName) {
            testLogger.info("Received onConsumerPause for consumer "+consumerName)
            consumerStatusEvents.add(new ConsumerStatus(consumerName, true))
        }

        @Override
        void onConsumerResume(@NotNull String consumerName) {
            testLogger.info("Received onConsumerResume for consumer "+consumerName)
            consumerStatusEvents.add(new ConsumerStatus(consumerName, false))
        }
    }
}
