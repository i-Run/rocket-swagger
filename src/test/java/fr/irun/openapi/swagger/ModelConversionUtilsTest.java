package fr.irun.openapi.swagger;

import fr.irun.openapi.swagger.ModelConversionUtils;
import fr.irun.openapi.swagger.mock.ParameterizedTypeMock;
import fr.irun.openapi.swagger.mock.TypeMock;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ModelConversionUtilsTest {

    @Test
    void testIsHexamonEntity() {
        assertThat(ModelConversionUtils.isHexamonEntityType(new TypeMock("fr.irun.hexamon.api.entity.Entity")))
                .isTrue();
        assertThat(ModelConversionUtils.isHexamonEntityType(new TypeMock("fr.irun.openapi.swagger.mock.GenericMock")))
                .isFalse();
        assertThat(ModelConversionUtils.isHexamonEntityType(new TypeMock("fr.irun.openapi.swagger.mock.SimpleMock")))
                .isFalse();
        final String genericWithEntityType = "fri.irun.openapi.swagger.GenericType<fr.irun.hexamon.api.entity.Entity>";
        assertThat(ModelConversionUtils.isHexamonEntityType(new TypeMock(genericWithEntityType)))
                .isFalse();
        assertThat(ModelConversionUtils.isHexamonEntityType(null)).isFalse();
    }

    @Test
    void testGetFullClassName() {
        assertThat(ModelConversionUtils.getFullClassName(new TypeMock("fr.irun.openapi.swagger.MyType")))
                .isEqualTo("fr.irun.openapi.swagger.MyType");
        assertThat(ModelConversionUtils.getFullClassName(new TypeMock("fr.irun.openapi.swagger.MyType<r.irun.openapi.swagger.MyOtherType>")))
                .isEqualTo("fr.irun.openapi.swagger.MyType");
        assertThat(ModelConversionUtils.getFullClassName(new TypeMock("[simple type, fr.irun.openapi.swagger.MyType]")))
                .isEqualTo("fr.irun.openapi.swagger.MyType");
        assertThat(ModelConversionUtils.getFullClassName(null)).isEmpty();
    }

    @Test
    void testIsDateTime() {
        assertThat(ModelConversionUtils.isDateType(Instant.class)).isTrue();
        assertThat(ModelConversionUtils.isDateType(LocalDateTime.class)).isTrue();
        // LocalDateTime and Instant only
        assertThat(ModelConversionUtils.isDateType(Date.class)).isFalse();
        assertThat(ModelConversionUtils.isDateType(java.sql.Date.class)).isFalse();
        assertThat(ModelConversionUtils.isDateType(String.class)).isFalse();
        assertThat(ModelConversionUtils.isDateType(null)).isFalse();
    }

    @Test
    void testDoesTypeMatchAnyClass() {
        assertThat(ModelConversionUtils.doesTypeMatchAnyClass(Flux.class, Flux.class)).isTrue();
        assertThat(ModelConversionUtils.doesTypeMatchAnyClass(String.class, Flux.class)).isFalse();
        assertThat(ModelConversionUtils.doesTypeMatchAnyClass(String.class, Integer.class, String.class)).isTrue();
        assertThat(ModelConversionUtils.doesTypeMatchAnyClass(null, Integer.class)).isFalse();
    }

    @Test
    void testExtractGenericFirstInnerType() {
        ParameterizedTypeMock paramType = new ParameterizedTypeMock(String.class, Integer.class);
        assertThat(ModelConversionUtils.extractGenericFirstInnerType(paramType)).isEqualTo(String.class);

        ParameterizedTypeMock emptyParamType = new ParameterizedTypeMock();
        assertThat(ModelConversionUtils.extractGenericFirstInnerType(emptyParamType)).isNull();

        TypeMock notGenericType = new TypeMock("fr.irun.SomeClass");
        assertThat(ModelConversionUtils.extractGenericFirstInnerType(notGenericType)).isNull();

        assertThat(ModelConversionUtils.extractGenericFirstInnerType(null)).isNull();
    }

}
