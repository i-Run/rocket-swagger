package fr.irun.openapi.swagger.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ResolutionStrategyTest {


    private static Stream<Arguments> fromClassNameParams() {
        return Stream.of(
                Arguments.of("org.springframework.http.ResponseEntity", ResolutionStrategy.WRAP_GENERIC),
                Arguments.of(Mono.class.getName(), ResolutionStrategy.WRAP_GENERIC),
                Arguments.of(Flux.class.getName(), ResolutionStrategy.WRAP_GENERIC_ARRAY),
                Arguments.of(Instant.class.getName(), ResolutionStrategy.DATE_TIME),
                Arguments.of(LocalDateTime.class.getName(), ResolutionStrategy.DATE_TIME),
                Arguments.of(java.util.Date.class.getName(), ResolutionStrategy.DATE_TIME),
                Arguments.of(java.sql.Date.class.getName(), ResolutionStrategy.DATE_TIME),
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