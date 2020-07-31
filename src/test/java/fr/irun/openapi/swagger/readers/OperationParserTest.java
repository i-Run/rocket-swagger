package fr.irun.openapi.swagger.readers;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Stream;

class OperationParserTest {
    @ParameterizedTest
    @MethodSource("should_merge_api_responses")
    void should_merge_api_responses(List<ApiResponses> toBeMerged, ApiResponses expected) {
        ApiResponses actual = OperationParser.mergeApiResponses(toBeMerged.toArray(new ApiResponses[]{}));
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> should_merge_api_responses() {
        return Stream.of(
                Arguments.of(ImmutableList.of(
                        new ApiResponses()
                                .addApiResponse(ApiResponses.DEFAULT, new ApiResponse().description("Description Default")),
                        new ApiResponses()
                                .addApiResponse(ApiResponses.DEFAULT, new ApiResponse()
                                        .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new Schema<>().description("desc Schema").name("String")))))
                                .addApiResponse("404", new ApiResponse()
                                        .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new Schema<>().description("desc Schema").name("Exception")))))
                        ),
                        new ApiResponses()
                                .addApiResponse(ApiResponses.DEFAULT, new ApiResponse()
                                        .description("Description Default")
                                        .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new Schema<>().description("desc Schema").name("String")))))
                                .addApiResponse("404", new ApiResponse()
                                        .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                                .schema(new Schema<>().description("desc Schema").name("Exception")))))

                ));
    }

    @ParameterizedTest
    @CsvSource({
            "/, /, true",
            "/test, /tset, false",
            "test, /test, true",
            "test/, /test, true",
            "test, test/, true",
            ", /, false",
            ", , true",
            "/test, , false",
            "null, test, false",
            "test, null, false",
            "null, null, true",
    })
    void should_test_if_same_path(String left, String right, String expected) {
        boolean actual = OperationParser.isSamePath(left, right);
        Assertions.assertThat(actual).isEqualTo(Boolean.parseBoolean(expected));
    }
}