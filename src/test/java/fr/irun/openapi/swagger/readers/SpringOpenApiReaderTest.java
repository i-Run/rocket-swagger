package fr.irun.openapi.swagger.readers;

import fr.irun.openapi.swagger.samples.SimpleRestController;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.irun.openapi.swagger.readers.SpringOpenApiReader.DEFAULT_MEDIA_TYPE_VALUE;
import static fr.irun.openapi.swagger.readers.SpringOpenApiReader.DEFAULT_RESPONSE_STATUS;

class SpringOpenApiReaderTest {

    private SpringOpenApiReader tested;

    @BeforeEach
    void setUp() {
        tested = new SpringOpenApiReader(new OpenAPI());
    }

    @Test
    void should_add_default_response_if_error_responses_exists() {
        OpenAPI actual = tested.read(SimpleRestController.class);
        Assertions.assertThat(actual).isNotNull();
        {
            ApiResponse aDefault = actual.getPaths().get("/listStringsWithoutDefaultApiResponse").getGet()
                    .getResponses().get(DEFAULT_RESPONSE_STATUS);
            Assertions.assertThat(aDefault).isNotNull();
            Assertions.assertThat(aDefault.getContent().get(DEFAULT_MEDIA_TYPE_VALUE).getSchema().getType())
                    .isEqualTo("array");
        }
        {
            ApiResponse aDefault = actual.getPaths().get("/listStringsWithIncompleteDefaultAdiResponse").getGet()
                    .getResponses().get(DEFAULT_RESPONSE_STATUS);
            Assertions.assertThat(aDefault).isNotNull();
            Assertions.assertThat(aDefault.getDescription()).isEqualTo("The default response description");
            Assertions.assertThat(aDefault.getContent().get(DEFAULT_MEDIA_TYPE_VALUE).getSchema().getType())
                    .isEqualTo("array");
        }
    }
}