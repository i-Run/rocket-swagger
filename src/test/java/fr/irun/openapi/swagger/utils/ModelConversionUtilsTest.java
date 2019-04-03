package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import fr.irun.openapi.swagger.mock.EntityMock;
import fr.irun.openapi.swagger.mock.ParameterizedTypeMock;
import fr.irun.openapi.swagger.mock.TypeMock;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ModelConversionUtilsTest {

    @Test
    void testIsDateTime() {
        assertThat(ModelConversionUtils.isDateType(Instant.class)).isTrue();
        assertThat(ModelConversionUtils.isDateType(LocalDateTime.class)).isTrue();
        // LocalDateTime and Instant only
        assertThat(ModelConversionUtils.isDateType(Date.class)).isTrue();
        assertThat(ModelConversionUtils.isDateType(java.sql.Date.class)).isTrue();
        assertThat(ModelConversionUtils.isDateType(String.class)).isFalse();
        assertThat(ModelConversionUtils.isDateType(null)).isFalse();
    }

    @Test
    void testExtractGenericFirstInnerTypeParameterizedType() {
        ParameterizedTypeMock paramType = new ParameterizedTypeMock("", String.class, Integer.class);
        assertThat(ModelConversionUtils.extractGenericFirstInnerType(paramType)).isEqualTo(String.class);

        ParameterizedTypeMock emptyParamType = new ParameterizedTypeMock("");
        assertThat(ModelConversionUtils.extractGenericFirstInnerType(emptyParamType)).isNull();

        TypeMock notGenericType = new TypeMock("fr.irun.SomeClass");
        assertThat(ModelConversionUtils.extractGenericFirstInnerType(notGenericType)).isNull();

        assertThat(ModelConversionUtils.extractGenericFirstInnerType(null)).isNull();
    }


    @Test
    void testExtractGenericFirstInnerTypeSimpleType() {
        final JavaType innerType = TypeFactory.defaultInstance().constructType(Integer.class);
        final Type genericType = TypeFactory.defaultInstance().constructSimpleType(Mono.class, new JavaType[]{innerType});

        final Type notNullType = ModelConversionUtils.extractGenericFirstInnerType(genericType);
        assertThat(notNullType).isNotNull();
        assertThat(notNullType).isEqualTo(innerType);

        final Type nonGenericType = TypeFactory.defaultInstance().constructSimpleType(String.class, new JavaType[0]);
        assertThat(ModelConversionUtils.extractGenericFirstInnerType(nonGenericType)).isNull();
    }


    @Test
    void copyModel() {
        Model baseModel = new RefModel();
        baseModel.setDescription("Some description");
        baseModel.setReference("Some reference");
        baseModel.setTitle("Some title");
        baseModel.setExample("Some example");

        ModelImpl model = ModelConversionUtils.copyModel("Some new model name", "Some new model reference", baseModel);
        assertThat(model).isNotNull();
        assertThat(model.getDescription()).isEqualTo("Some description");
        assertThat(model.getReference()).isEqualTo("Some new model reference");
        assertThat(model.getExample()).isEqualTo("Some example");
        assertThat(model.getTitle()).isEqualTo("Some title");
        assertThat(model.getName()).isEqualTo("Some new model name");
    }

    @Test
    void copyNullModel() {
        ModelImpl model = ModelConversionUtils.copyModel("", "", null);
        assertThat(model).isNotNull();
    }

    @Test
    void computeModelType() {
        final Type monoType = new TypeMock("[Simple class, " + Mono.class.getName() + "]");
        final Type fluxType = new TypeMock("[Simple class, " + Flux.class.getName() + "]");
        final Type entityType = new TypeMock("[Simple class, fr.irun.hexamon.api.entity.Entity]");
        final Type nestedType = new TypeMock("[Simple class, fr.irun.cms.api.model.Nested]");
        final Type stringType = new TypeMock("[Simple class, " + String.class.getName() + "]");
        final Type localeType = new TypeMock("[Simple class, " + Locale.class.getName() + "]");
        final Type dateType = new TypeMock("[Simple class, " + Instant.class.getName() + "]");

        assertThat(ModelConversionUtils.computeModelType(monoType)).isEqualTo(ModelEnum.MONO);
        assertThat(ModelConversionUtils.computeModelType(fluxType)).isEqualTo(ModelEnum.FLUX);
        assertThat(ModelConversionUtils.computeModelType(entityType)).isEqualTo(ModelEnum.ENTITY);
        assertThat(ModelConversionUtils.computeModelType(nestedType)).isEqualTo(ModelEnum.NESTED);
        assertThat(ModelConversionUtils.computeModelType(stringType)).isEqualTo(ModelEnum.STANDARD);
        assertThat(ModelConversionUtils.computeModelType(localeType)).isEqualTo(ModelEnum.STANDARD);
        assertThat(ModelConversionUtils.computeModelType(dateType)).isEqualTo(ModelEnum.STANDARD);
    }


    @Test
    void extractInnerTypesReversed() {
        final Type stringType = new TypeMock(String.class.getName());
        final Type entityStringType = new ParameterizedTypeMock(EntityMock.class.getName(), stringType);
        final Type fluxEntityStringType = new ParameterizedTypeMock(Flux.class.getName(), entityStringType);

        assertThat(ModelConversionUtils.extractInnerTypesReversed(fluxEntityStringType))
                .containsExactly(stringType, entityStringType, fluxEntityStringType);
        assertThat(ModelConversionUtils.extractInnerTypesReversed(entityStringType))
                .containsExactly(stringType, entityStringType);
        assertThat(ModelConversionUtils.extractInnerTypesReversed(stringType))
                .containsExactly(stringType);
    }

    @Test
    void extractLastSplitResult() {
        assertThat(ModelConversionUtils.extractLastSplitResult("#/definitions/SomeValue", "/"))
                .isEqualTo("SomeValue");
        assertThat(ModelConversionUtils.extractLastSplitResult("#/definitions/////SomeValue", "/"))
                .isEqualTo("SomeValue");
        assertThat(ModelConversionUtils.extractLastSplitResult("SomeValue", "/"))
                .isEqualTo("SomeValue");
        assertThat(ModelConversionUtils.extractLastSplitResult("", "/"))
                .isEqualTo("");
        assertThat(ModelConversionUtils.extractLastSplitResult(null, "/"))
                .isEqualTo("");
    }

    @Test
    void isUnresolvableType() {
        assertThat(ModelConversionUtils.isUnresolvableType(JsonNode.class)).isTrue();
        assertThat(ModelConversionUtils.isUnresolvableType(new TypeMock("[Simple type, " + JsonNode.class + "]"))).isTrue();
        assertThat(ModelConversionUtils.isUnresolvableType(EntityMock.class)).isFalse();
    }

}
