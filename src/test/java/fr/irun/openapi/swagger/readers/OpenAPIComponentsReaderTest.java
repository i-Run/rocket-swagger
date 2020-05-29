package fr.irun.openapi.swagger.readers;

import fr.irun.openapi.swagger.samples.AuthenticationController;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;

class OpenAPIComponentsReaderTest {

    @Test
    void should_read_security_scheme() {
        Map<String, SecurityScheme> actual = OpenAPIComponentsReader.readSecuritySchemes(AuthenticationController.class);
        Assertions.assertThat(actual)
                .isNotNull()
                .isNotEmpty()
                .containsKey(HttpHeaders.AUTHORIZATION);
    }
}