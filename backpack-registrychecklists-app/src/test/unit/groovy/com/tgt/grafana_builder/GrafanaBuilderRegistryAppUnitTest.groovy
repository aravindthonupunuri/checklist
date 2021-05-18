package com.tgt.grafana_builder

import spock.lang.Specification

class GrafanaBuilderRegistryAppUnitTest extends Specification {

    def "build backpack-registrychecklists-app grafana dashboard"() {
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

        def backpackRegistryClient = new GrafanaBuilderConfig.ApiClient(
            baseUriPath: "/registries/v2",
            apiMethods: [
                new GrafanaBuilderConfig.ApiMethod(
                    panelTitle: "Get Registry Details",
                    methodName: "GET",
                    pathUri: "/{registry_id}/summary_details"
                )
            ]
        )

        def apiClients = [
            redskyClient,
            backpackRegistryClient
        ]

        def metricsAlert = new GrafanaBuilderConfig.MetricsAlert(
            prodTapApplication: "backpackregistrychecklists",
            prodTapCluster: "backpackregistrychecklists",
            notificationUids: [ "FQW_lvBZk", "roC6asiMz", "7GGtGwmMz" ],
            cpuUsageThreshold: "75",
            memUsageThreshold: "75",
            server500countThreshold: 25
        )

        def kafkaProducers = [
            new GrafanaBuilderConfig.KafkaProducer(
                title: "Registry msgbus producer",
                metricsName: "msgbus_producer_event",
                isDlqProducer: false
            )
        ]

        GrafanaBuilderConfig grafanaBuilderConfig = new GrafanaBuilderConfig(
            tapDashboardJsonFile: "${moduleDir}/src/test/unit/resources/tap-dashboard.json",
            apiServerSpecBasePath: "/registries_checklists/v1",
            apiServerSpecPath: "${moduleDir}/api-specs/backpack-registrychecklists-v1.yml",
            httpClientRowTitle: "Outbound Http Clients",
            apiClients: apiClients,
            needResiliencePanel: true,
            metricsAlert: metricsAlert,
            kafkaProducers: kafkaProducers,
            postgres: true
        )

        GrafanaBuilder grafanaBuilder = new GrafanaBuilder(grafanaBuilderConfig)

        when:
        def success = grafanaBuilder.buildPanels()

        then:
        success
    }
}
