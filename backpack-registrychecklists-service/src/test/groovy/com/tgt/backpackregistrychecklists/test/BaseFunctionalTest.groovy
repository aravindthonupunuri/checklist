package com.tgt.backpackregistrychecklists.test

import com.tgt.lists.micronaut.test.MockServer
import com.tgt.lists.micronaut.test.MockServerDelegate
import com.tgt.lists.msgbus.ListsMessageBusProducer
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventLifecycleNotificationProvider
import com.tgt.lists.msgbus.metrics.MetricsPublisher
import com.tgt.lists.msgbus.producer.MsgbusKafkaProducerClient
import com.tgt.lists.msgbus.tracing.EventTracer
import com.tgt.lists.msgbus.tracing.ZipkinTracingMapper
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MockClock
import io.micrometer.core.instrument.simple.SimpleConfig
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micronaut.context.annotation.Value
import io.micronaut.context.env.Environment
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.support.TestPropertyProvider
import io.opentracing.mock.MockTracer
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

abstract class BaseFunctionalTest extends Specification implements TestPropertyProvider {

    static Logger baseLogger = LoggerFactory.getLogger(BaseFunctionalTest)

    @Inject
    @Client("/")
    RxHttpClient client

    @Inject
    MockServerDelegate mockServerDelegate

    MockServer mockServer

    String listType = "REGISTRY"

    @Value("\${msgbus.dlq-source}")
    String dlqSource

    abstract Logger getLogger()

    def setup() {
        mockServer = Mock(MockServer)
        mockServer.post({ path -> path.contains("/auth/oauth/v2/token") }, _, _) >> [status: 200, body: DataProvider.getTokenResponse()]
        mockServer.post({ path -> path.contains("/api/v2/spans") }, _, _) >> [status: 200, body: []]

        mockServerDelegate.delegate = mockServer
    }

    @Override
    Map<String, String> getProperties() {
        int httpServerPort = SocketUtils.findAvailableTcpPort()
        baseLogger.info("Using HTTP server-port: $httpServerPort")
        Map<String, String> properties = [
            "serverPort" : httpServerPort,
            "tracing.zipkin.enabled": true,
            "tracing.zipkin.sample-rate-percent": 100
        ]
        System.setProperty("APP_UUID", UUID.randomUUID().toString())
        return properties
    }

    ListsMessageBusProducer newMockMsgbusKafkaProducerClient(EventLifecycleNotificationProvider eventNotificationsProvider) {
        MeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
        EventHeaderFactory eventHeaderFactory = new EventHeaderFactory(2, 5, dlqSource)
        def mockedEnvironment = Mock(Environment)
        1 * mockedEnvironment.containsProperties(_) >> true
        return new ListsMessageBusProducer("testcLient", "dummySrc", "dummyTopic", eventHeaderFactory, new MsgbusKafkaProducerClient() {
            @Override
            Mono<RecordMetadata> sendEvent(Object partitionKey, @NotNull Object data, @NotNull byte[] eventId, @NotNull byte[] eventType,
                                           @Nullable byte[] correlationId, @Nullable byte[] timestamp,
                                           @Nullable byte[] errorCode, @Nullable byte[] errorMessage,
                                           @Nullable byte[] retryCount, @Nullable byte[] retryTimestamp, @Nullable byte[] maxRetryCount,
                                           @Nullable byte[] source, @Nullable byte[] traceHeader, @Nullable byte[] mdcHeader,
                                           @Nullable byte[] testMode) {
                def metadata = new RecordMetadata(new TopicPartition("dummy", 1), 1, 1, System.currentTimeMillis(),
                    1, 1, 1)
                return Mono.just(metadata)
            }
        }, eventNotificationsProvider, new EventTracer(new MockTracer(), new ZipkinTracingMapper(), null, false), new MetricsPublisher(meterRegistry), null, mockedEnvironment )
    }
}
