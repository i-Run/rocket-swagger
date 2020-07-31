package fr.irun.openapi.swagger.utils;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Schema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class OpenAPIComponentsHelperTest {

    @Test
    void should_merge_openapi_components() {
        Components expected = new Components()
                .links(ImmutableMap.of("link1", new Link().$ref("ref1")))
                .schemas(ImmutableMap.of(
                        "schema1", new Schema<String>().$ref("ref1").description("schem 1"),
                        "schema2", new Schema<String>().$ref("ref2").description("schem 2")))
                .headers(ImmutableMap.of("header1", new Header().description("header 1")));

        Components actual = OpenAPIComponentsHelper.mergeAllComponents(
                new Components().links(expected.getLinks()),
                new Components().links(expected.getLinks())
                        .schemas(ImmutableMap.of("schema1", expected.getSchemas().get("schema1"))),
                new Components().schemas(expected.getSchemas()).headers(expected.getHeaders()),
                null
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parametersComponentsEmpty")
    void should_test_empty_or_null(Components components, boolean expected) {
        boolean actual = OpenAPIComponentsHelper.isNullOrEmpty(components);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> parametersComponentsEmpty() {
        return Stream.of(
                Arguments.of(new Components().links(ImmutableMap.of("link1", new Link().$ref("ref1"))), false),
                Arguments.of(new Components().headers(ImmutableMap.of("header1", new Header().description("header 1"))), false),
                Arguments.of(new Components().schemas(ImmutableMap.of()), true),
                Arguments.of(null, true)
        );
    }

    @Test
    void should_instantiate_new_map() {
        Assertions.assertThat(OpenAPIComponentsHelper.ALL_COMPONENTS_TYPE)
                .extracting(OpenAPIComponentsHelper::newMap)
                .isNotNull();
    }
}