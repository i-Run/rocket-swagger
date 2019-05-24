package fr.irun.openapi.swagger.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ModelEnumTest {


    private static Stream<Arguments> fromClassNameParams() {
        return Stream.of(
                Arguments.of(Mono.class.getName(), ModelEnum.MONO),
                Arguments.of(Flux.class.getName(), ModelEnum.FLUX),
                Arguments.of("DummyClass", ModelEnum.STANDARD),
                Arguments.of(null, ModelEnum.STANDARD)
        );
    }

    @ParameterizedTest
    @MethodSource("fromClassNameParams")
    void fromClassName(String className, ModelEnum expectedType) {
        final ModelEnum actual = ModelEnum.fromClassName(className);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expectedType);
    }
}