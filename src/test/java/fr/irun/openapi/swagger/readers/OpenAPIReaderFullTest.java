package fr.irun.openapi.swagger.readers;

import com.google.common.io.CharStreams;
import fr.irun.openapi.swagger.samples.AuthenticationController;
import fr.irun.openapi.swagger.samples.RestWithBodyController;
import fr.irun.openapi.swagger.samples.RestWithConsumesController;
import fr.irun.openapi.swagger.samples.SimpleRestController;
import fr.irun.openapi.swagger.samples.SimpleRestWithParameters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class OpenAPIReaderFullTest {
    private SpringOpenApiReader tested;

    @BeforeEach
    void setUp() {
        tested = new SpringOpenApiReader(new OpenAPI()
                .info(new Info()
                        .title("test")
                        .version("1.0.0")));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            RestWithBodyController.class,
            RestWithConsumesController.class,
            SimpleRestController.class,
            SimpleRestWithParameters.class,
            AuthenticationController.class
    })
    void should_generate_yaml_for_class(Class<?> clazz) throws IOException {
        OpenAPI openAPI = tested.read(clazz);

        String actual = Json.pretty(openAPI);

        Assertions.assertThat(actual).isNotEmpty();

        InputStream resourceAsStream = OpenAPIReaderFullTest.class.getClassLoader().getResourceAsStream("openapi-jsons/" + clazz.getSimpleName() + ".json");
        Assertions.assertThat(resourceAsStream).isNotNull();
        String expectedJson = CharStreams.toString(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
        JsonAssertions.assertThatJson(actual).isEqualTo(expectedJson);
    }
}
