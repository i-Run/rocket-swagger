package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test case for the class {@link JacksonFactory}
 */
class JacksonFactoryTest {

    @Test
    void buildObjectMapper_should_have_mixin() {
        final ObjectMapper actualMapper = JacksonFactory.buildObjectMapper();
        assertThat(actualMapper).isNotNull();
    }
}