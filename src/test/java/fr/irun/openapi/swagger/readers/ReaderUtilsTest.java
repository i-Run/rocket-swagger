package fr.irun.openapi.swagger.readers;

import com.google.common.collect.Iterators;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.stream.Stream;

class ReaderUtilsTest {
    private static Stream<Arguments> allMethodAnnotated() {
        return Stream.of(ClazzWithAnnotatedMethod.class.getDeclaredMethods())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("allMethodAnnotated")
    void should_extract_operation_from_method(Method method) {
        String actual = ReaderUtils.extractOperationMethod(method, Iterators.forArray());
        Assertions.assertThat(actual).isEqualTo("get");
    }

    public static class ClazzWithAnnotatedMethod {
        @RequestMapping(method = RequestMethod.GET)
        public void methodWithRequestMapping() {
        }

        @GetMapping
        public void methodWithGetMapping() {
        }
    }
}