package fr.irun.openapi.swagger.readers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.utils.ReaderUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.stream.Stream;

class ReaderUtilsTest {
    @ParameterizedTest
    @MethodSource("allMethodAnnotated")
    void should_extract_operation_from_method(Method method) {
        String actual = ReaderUtils.extractOperationMethod(method, Iterators.forArray());
        Assertions.assertThat(actual).isEqualTo("get");
    }

    @ParameterizedTest(name = "[{index}] {4}")
    @MethodSource("should_get_path_parameters")
    void should_get_path(RequestMapping clazzMapping, RequestMapping methodMapping,
                         String parentPath, boolean isSubResource, String expectedName) {
        String actual = ReaderUtils.getPath(clazzMapping, methodMapping, parentPath, isSubResource);
        Assertions.assertThat(actual).isEqualTo(expectedName);
    }

    private static Stream<Arguments> allMethodAnnotated() {
        return Stream.of(ClazzWithAnnotatedMethod.class.getDeclaredMethods())
                .map(Arguments::of);
    }

    private static Stream<Arguments> should_get_path_parameters() {
        RequestMapping clazzMapping = AnnotatedElementUtils.findMergedAnnotation(ClazzWithAnnotatedMethod.class, RequestMapping.class);
        assert clazzMapping != null;
        return Stream.concat(
                Stream.of(ClazzWithAnnotatedMethod.class.getDeclaredMethods())
                        .map(m -> Arguments.of(
                                clazzMapping,
                                AnnotatedElementUtils.findMergedAnnotation(m, RequestMapping.class),
                                "/",
                                true,
                                "/" + m.getName())),
                Stream.of(ClazzWithAnnotatedMethod.class.getDeclaredMethods())
                        .map(m -> Arguments.of(
                                clazzMapping,
                                AnnotatedElementUtils.findMergedAnnotation(m, RequestMapping.class),
                                "/",
                                false,
                                "/test/" + m.getName()))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/my, false",
            "/my/first/test, false",
            "/my/first/route, true",
            "/my/second/route, true",
            "/my/second/test, true",
    })
    void should_check_ignored_path(String path, boolean expectedIgnore) {
        ImmutableSet<String> ignoredRoutes = ImmutableSet.of(
                "/my/first/route",
                "/my/second"
        );
        Assertions.assertThat(ReaderUtils.isIgnored(path, ignoredRoutes)).isEqualTo(expectedIgnore);
    }

    @RequestMapping("/test")
    public static class ClazzWithAnnotatedMethod {
        @RequestMapping(path = "/methodWithRequestMapping", method = RequestMethod.GET)
        public void methodWithRequestMapping() {
        }

        @GetMapping("/methodWithGetMapping")
        public void methodWithGetMapping() {
        }
    }
}