package com.tgt.backpackregistrychecklists.api

import com.tgt.swagger_sync.ApiSpec
import com.tgt.swagger_sync.OpenApi3Parser
import com.tgt.swagger_sync.SpecComparator
import com.tgt.swagger_sync.SpecVersionComparator
import com.tgt.swagger_sync.Swagger2Parser
import spock.lang.Specification

class ApiSpecTest extends Specification {

    static String staticSpecRelativePath = "api-specs/backpack-registrychecklists-v1.yml"
    static String dynamicSpecRelativePath = "/build/tmp/kapt3/classes/main/META-INF/swagger/backpack-registry-checklists-v1.yml"

    Swagger2Parser staticSpecFileParser
    OpenApi3Parser dynamicSpecFileParser

    def setup() {
        def appDir = System.getProperty("user.dir")

        String staticSpecFilePath = "${appDir}/${staticSpecRelativePath}"
        staticSpecFileParser = new Swagger2Parser(staticSpecFilePath)

        String dynamicSpecFilePath = "${appDir}${dynamicSpecRelativePath}"
        dynamicSpecFileParser = new OpenApi3Parser(dynamicSpecFilePath)
    }

    def "validate dynamically generated swagger spec against static spec for backpack-registry"() {
        given:
        ApiSpec staticSpec = staticSpecFileParser.parse()
        ApiSpec dynamicSpec = dynamicSpecFileParser.parse()

        when:
        SpecComparator specComparator = new SpecComparator(staticSpec, dynamicSpec)

        then:
        specComparator.match()
    }

    def "test spec version comparison with git master"() {
        given:
        SpecVersionComparator specVersionComparator = new SpecVersionComparator("backpack-registrychecklists-app", staticSpecRelativePath)

        when:
        def result = specVersionComparator.match()

        then:
        result
    }
}
