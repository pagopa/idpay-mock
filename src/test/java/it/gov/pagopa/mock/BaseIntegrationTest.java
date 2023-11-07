package it.gov.pagopa.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import it.gov.pagopa.common.mongo.MongoTestUtilitiesService;
import it.gov.pagopa.common.mongo.singleinstance.AutoConfigureSingleInstanceMongodb;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@TestPropertySource(
        properties = {
                // even if enabled into application.yml, spring test will not load it
                // https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.spring-boot-applications.jmx
                "spring.jmx.enabled=true",

                // region mongodb
                "logging.level.org.mongodb.driver=WARN",
                "logging.level.de.flapdoodle.embed.mongo.spring.autoconfigure=WARN",
                "de.flapdoodle.mongodb.embedded.version=4.2.24",
                // endregion

                //region pdv
                "logging.level.WireMock=ERROR",
                "rest-client.encryptpdv.baseUrl=http://localhost:${wiremock.server.port}",
                "api.key.encrypt=x_api_key",
                //endregion

        })
@AutoConfigureMockMvc
@AutoConfigureWireMock(stubs = "classpath:/stub", port = 0)
@AutoConfigureSingleInstanceMongodb
public abstract class BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private MongoTestUtilitiesService mongoTestUtilitiesService;
    @Autowired
    protected MongoTemplate mongoTemplate;
    @Autowired
    private WireMockServer wireMockServer;


    @PostConstruct
    public void logEmbeddedServerConfig() {
        System.out.printf("""
                        ************************
                        Embedded mongo: %s
                        Wiremock HTTP: http://localhost:%s
                        Wiremock HTTPS: %s
                        ************************
                        """,
                mongoTestUtilitiesService.getMongoUrl(),
                wireMockServer.getOptions().portNumber(),
                wireMockServer.baseUrl());
    }

}
