package fr.irun.openapi.swagger.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OperationIdProviderTest {

    private OperationIdProvider tested;

    @BeforeEach
    void setUp() {
        OpenAPI openAPI = new OpenAPI()
                .path("/login", new PathItem()
                        .get(new Operation().operationId("login"))
                        .post(new Operation().operationId("login_1")))
                .path("/users", new PathItem()
                        .get(new Operation().operationId("getUsers")));

        tested = new OperationIdProvider().load(openAPI);
    }

    @ParameterizedTest
    @CsvSource({
            "login, login_2",
            "obiwan, obiwan",
            "getUsers, getUsers_1"
    })
    void should_find_next_available_operation_id(String preferred, String expectedId) {
        String actual = tested.provideOperationId(preferred);
        Assertions.assertThat(actual).isEqualTo(expectedId);
    }

    @Test
    void should_get_operationid_from_empty_OpenAPI() {
        tested = new OperationIdProvider();
        Assertions.assertThat(tested.provideOperationId("login")).isEqualTo("login");
    }
}