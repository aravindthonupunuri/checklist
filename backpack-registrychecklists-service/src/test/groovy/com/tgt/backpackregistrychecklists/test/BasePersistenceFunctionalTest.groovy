package com.tgt.backpackregistrychecklists.test


import io.micronaut.test.support.TestPropertyProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared

import java.sql.DriverManager

abstract class BasePersistenceFunctionalTest extends BaseFunctionalTest implements TestPropertyProvider {

    static Logger baseLogger = LoggerFactory.getLogger(BasePersistenceFunctionalTest)

    @Shared
    static PostgreSQLContainer postgreSQLContainer

    static String jdbc

    def setupSpec() {
        if (this.class.name != BasePersistenceFunctionalTest.class.name) {
            truncate()
        }
    }

    def cleanupSpec() {
        if (this.class.name != BasePersistenceFunctionalTest.class.name) {
            truncate()
        }
    }

    /*
    These properties will override application.yml defined properties
    */
    @Override
    Map<String, String> getProperties() {
        def map = Object.getProperties()

        jdbc = System.getenv("JDBC_URL")

        if(jdbc == null) {
            if (postgreSQLContainer == null) {
                postgreSQLContainer = new PostgreSQLContainer()
                    .withDatabaseName("registrychecklists")
                    .withUsername("postgres")
                    .withPassword("postgres")
                postgreSQLContainer.start()
            }
            jdbc = postgreSQLContainer.jdbcUrl
            baseLogger.info("getProperties [postgreSQLContainer jdbc: $jdbc]")
        }
        else {
            baseLogger.info("getProperties [env JDBC_URL: $jdbc]")
        }

        baseLogger.info("getProperties [datasources.default.url: $jdbc]")

        def moduleDir = System.getProperty("user.dir")

        Map<String, String> properties = ["datasources.default.url" : jdbc,
                                          "datasources.default.driverClassName": "org.postgresql.Driver",
                                          "datasources.default.username": "postgres",
                                          "datasources.default.password": "postgres",
                                          "datasources.default.dialect": "POSTGRES",
                                          "datasources.default.poolName": "backpackregistryapiapp",
                                          "datasources.default.maximumPoolSize": "2",
                                          "datasources.default.minimumIdle": "1",
                                          "datasources.default.idleTimeout": "600000",
                                          "datasources.default.maxLifetime": "1800000",
                                          "datasources.default.connectionTimeout": "10000",
                                          "flyway.schemas": "grws",
                                          "flyway.datasources.default.locations": "filesystem:${moduleDir}/../backpack-registrychecklists-service/src/main/resources/db/migration/"]
        def additionalProperties = getAdditionalProperties()
        if (additionalProperties != null) {
            properties.putAll(additionalProperties)
        }
        map.putAll(properties)
        return map
    }

    /*
    Allow concrete functional test subclasses to add more properties
    These properties will override application.yml defined properties
     */
    Map<String, String> getAdditionalProperties() {
        return null
    }

    def truncate() {
        def connection = null
        def statement = null
        try {
            baseLogger.info("truncate [jdbc: $jdbc]")
            if (jdbc != null) {
                connection = DriverManager.getConnection(jdbc, "postgres", "postgres")
                statement = connection.createStatement()
                statement.executeUpdate("TRUNCATE TABLE REGISTRY_CHECKLIST, CHECKED_SUBCATEGORIES, CHECKLIST_TEMPLATE")
            }
        } catch(Throwable t) {
            t.printStackTrace()
        } finally {
            if(connection != null) {
                connection.close()
            }
            if(statement != null) {
                statement.close()
            }
        }
    }
}
