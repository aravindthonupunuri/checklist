package com.tgt.backpackregistrychecklists.kafka

import com.target.platform.connector.micronaut.PlatformPropertySource
import com.tgt.lists.common.components.tap.TAPEnvironmentLoader
import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {

        // TAP deployment specific
        TAPEnvironmentLoader().setupTAPSpecificEnvironment(listOf("backpack-registry-keystore", "grsclient-truststore"))

        Micronaut.build()
            .propertySources(PlatformPropertySource.connect())
            .packages("com.tgt.backpackregistrychecklists.kafka")
            .mainClass(Application.javaClass)
            .start()
    }
}
