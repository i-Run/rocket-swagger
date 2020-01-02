package fr.irun.openapi.swagger.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ResolutionStrategyTest {


    private static Stream<Arguments> fromClassNameParams() {
        return Stream.of(
                Arguments.of(Mono.class.getName(), ResolutionStrategy.WRAP_GENERIC),
                Arguments.of(Flux.class.getName(), ResolutionStrategy.WRAP_GENERIC_ARRAY),
                Arguments.of("DummyClass", ResolutionStrategy.DEFAULT),
                Arguments.of(null, ResolutionStrategy.DEFAULT)
        );
    }

    @ParameterizedTest
    @MethodSource("fromClassNameParams")
    void fromClassName(String className, ResolutionStrategy expectedType) {
        final ResolutionStrategy actual = ResolutionStrategy.fromClassName(className);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expectedType);
    }
}