package fr.irun.openapi.swagger.readers;

import com.google.common.collect.Iterables;
import fr.irun.openapi.swagger.samples.SimpleRestController;
import fr.irun.openapi.swagger.samples.SimpleRestWithParameters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

import java.util.List;

import static fr.irun.openapi.swagger.readers.SpringOpenApiReader.DEFAULT_DESCRIPTION;
import static fr.irun.openapi.swagger.readers.SpringOpenApiReader.DEFAULT_MEDIA_TYPE_VALUE;
import static fr.irun.openapi.swagger.readers.SpringOpenApiReader.DEFAULT_RESPONSE_STATUS;

class SpringOpenApiReaderTest {

    private SpringOpenApiReader tested;

    @BeforeEach
    void setUp() {
        tested = new SpringOpenApiReader(new OpenAPI());
    }

    @ParameterizedTest
    @CsvSource({
            "/listStringsWithoutDefaultApiResponse, 2, " + DEFAULT_DESCRIPTION + ", array",
            "/listStringsWithIncompleteDefaultAdiResponse, 2, The default response description, array",
            "/listStringsWithAnyAnnotations, 1, " + DEFAULT_DESCRIPTION + ", array",
    })
    void should_add_default_response_if_error_responses_exists(
            String route, int expectedResponses, String expectedDesc, String expectedType) {
        OpenAPI actual = tested.read(SimpleRestController.class);
        Assertions.assertThat(actual).isNotNull();
        ApiResponses responses = actual.getPaths().get(route).getGet().getResponses();
        Assertions.assertThat(responses).hasSize(expectedResponses);
        ApiResponse aDefault = responses.get(DEFAULT_RESPONSE_STATUS);
        Assertions.assertThat(aDefault).isNotNull();
        Assertions.assertThat(aDefault.getDescription()).isEqualTo(expectedDesc);
        Assertions.assertThat(aDefault.getContent().get(DEFAULT_MEDIA_TYPE_VALUE).getSchema().getType())
                .isEqualTo(expectedType);
    }

    @ParameterizedTest
    @CsvSource({
            "/parameterWithoutAnnotation/{propertyPath}"
    })
    void should_read_parameters_from_route_mapping(String route) {
        OpenAPI actual = tested.read(SimpleRestWithParameters.class);
        Assertions.assertThat(actual).isNotNull();
        List<Parameter> parameters = actual.getPaths().get(route).getGet().getParameters();
        Assertions.assertThat(parameters).hasSize(1);
        Assertions.assertThat(Iterables.getOnlyElement(parameters).getName()).isEqualTo("propertyPath");
    }

    @ParameterizedTest
    @CsvSource({
            "RestWithBodyController, /login, " + DEFAULT_MEDIA_TYPE_VALUE,
            "RestWithConsumesController, /consumeSameAsClass, " + MediaType.APPLICATION_XML_VALUE,
            "RestWithConsumesController, /consumeStream, " + MediaType.APPLICATION_OCTET_STREAM_VALUE,
    })
    void should_read_request_body(String ctrlClass, String route, String mediaType) throws ClassNotFoundException {
        OpenAPI actual = tested.read(Class.forName("fr.irun.openapi.swagger.samples." + ctrlClass));
        Assertions.assertThat(actual).isNotNull();
        Content content = actual.getPaths().get(route).getPost().getRequestBody().getContent();
        Assertions.assertThat(content.keySet()).containsExactly(mediaType);
    }

}