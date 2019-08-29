package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.irun.hexamon.api.entity.Entity;
import fr.irun.hexamon.infra.jackson.EntityJacksonMixin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test case for the class {@link JacksonFactory}
 */
class JacksonFactoryTest {


    private static Stream<Arguments> mapperMixinParams() {
        return Stream.of(
                Arguments.of(Entity.class, EntityJacksonMixin.class)
        );
    }

    @ParameterizedTest
    @MethodSource("mapperMixinParams")
    void buildObjectMapper_should_have_mixin(Class<?> baseClass, Class<?> expectedMixinClass) {
        final ObjectMapper actualMapper = JacksonFactory.buildObjectMapper();
        assertThat(actualMapper).isNotNull();

        final Class<?> actualEntityMixin = actualMapper.findMixInClassFor(baseClass);
        assertThat(actualEntityMixin).isNotNull();
        assertThat(actualEntityMixin).isEqualTo(expectedMixinClass);
    }
}