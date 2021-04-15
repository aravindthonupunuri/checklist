package com.tgt.grafana_builder

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class GrafanaBuilderMigrationConsumerUnitTest extends Specification {

    def "build backpack-registry-checklists-migration-consumer-app grafana dashboard"() {
        given:
        def moduleDir = System.getProperty("user.dir")

        def redskyClient = new GrafanaBuilderConfig.ApiClient(
            baseUriPath: "/redsky_aggregations/v1/registry_services",
            apiMethods: [
                new GrafanaBuilderConfig.ApiMethod(
                    panelTitle: "Redsky checklist",
                    methodName: "GET",
                    pathUri: "/get_registry_checklist_v1"
                )
            ]
        )

        def apiClients = [
            redskyClient
        ]

        def metricsAlert = new GrafanaBuilderConfig.MetricsAlert(
            prodTapApplication: "backpackregistrychecklistsmigrationconsumer",
            prodTapCluster: "backpackregistrychecklistsmigrationconsumer",
            notificationUids: [ "FQW_lvBZk", "roC6asiMz", "7GGtGwmMz" ],
            cpuUsageThreshold: "75",
            memUsageThreshold: "75",
            server500countThreshold: 25
        )

        def kafkaConsumers = [
            new GrafanaBuilderConfig.KafkaConsumer(
                title: "Migration Consumer",
                metricsName: "msgbus_consumer_event",
                isDlqConsumer: false,
                devEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-dev",
                    consumerGroup: "backpack-registrychecklists-migration-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                stageEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-stage",
                    consumerGroup: "backpack-registrychecklists-migration-data-bus-stage-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                prodEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-prod",
                    consumerGroup: "backpack-registrychecklists-migration-data-bus-prod-consumer",
                    ttcCluster: "ost-ttc-prod-app'",
                    tteCluster: "ost-ttce-prod-app'"
                )
            ),
            new GrafanaBuilderConfig.KafkaConsumer(
                title: "Migration DLQ Consumer",
                metricsName: "msgbus_consumer_event",
                isDlqConsumer: true,
                devEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-dev-dlq",
                    consumerGroup: "backpack-registrychecklists-migration-dlq-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                stageEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-stage-dlq",
                    consumerGroup: "backpack-registrychecklists-migration-dlq-stage-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                prodEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-prod-dlq",
                    consumerGroup: "backpack-registrychecklists-migration-dlq-prod-consumer",
                    ttcCluster: "ost-ttc-prod-app'",
                    tteCluster: "ost-ttce-prod-app'"
                )
            )
        ]

        def kafkaProducers = [
            new GrafanaBuilderConfig.KafkaProducer(
                title: "Registry msgbus DLQ Producer",
                metricsName: "msgbus_producer_event",
                isDlqProducer: true
            )
        ]

        GrafanaBuilderConfig grafanaBuilderConfig = new GrafanaBuilderConfig(
            tapDashboardJsonFile: "${moduleDir}/src/test/unit/resources/tap-dashboard.json",
            httpClientRowTitle: "Outbound Http Clients",
            apiClients: apiClients,
            needResiliencePanel: true,
            kafkaConsumers: kafkaConsumers,
            kafkaProducers: kafkaProducers,
            metricsAlert: metricsAlert,
            postgres: true
        )

        GrafanaBuilder grafanaBuilder = new GrafanaBuilder(grafanaBuilderConfig)

        when:
        def success = grafanaBuilder.buildPanels()

        then:
        success
    }
}

