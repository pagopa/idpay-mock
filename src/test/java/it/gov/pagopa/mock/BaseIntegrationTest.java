package it.gov.pagopa.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.mongo.repository.MongoTestUtilitiesService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
                "de.flapdoodle.mongodb.embedded.version=4.0.21",
                // endregion
        })
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MongoTestUtilitiesService mongoTestUtilitiesService;


    @Autowired
    protected MongoTemplate mongoTemplate;


    @PostConstruct
    public void logEmbeddedServerConfig() {
        System.out.printf("""
                        ************************
                        Embedded mongo: %s
                        ************************
                        """,
                mongoTestUtilitiesService.getMongoUrl());
    }

}
