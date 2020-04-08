package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import io.swagger.v3.core.converter.AnnotatedType;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelConversionUtilsTest {

    private static Stream<Arguments> params_should_get_resolution_strategy() {
        return Stream.of(
                Arguments.of(Mono.class, ResolutionStrategy.WRAP_GENERIC),
                Arguments.of(Flux.class, ResolutionStrategy.WRAP_GENERIC_ARRAY),
                Arguments.of(JsonNode.class, ResolutionStrategy.MAP),
                Arguments.of(String.class, ResolutionStrategy.DEFAULT),
                Arguments.of(Instant.class, ResolutionStrategy.DATE_TIME),
                Arguments.of(LocalDateTime.class, ResolutionStrategy.DATE_TIME),
                Arguments.of(java.util.Date.class, ResolutionStrategy.DATE_TIME),
                Arguments.of(java.sql.Date.class, ResolutionStrategy.DATE_TIME)
        );
    }


    @ParameterizedTest
    @MethodSource("params_should_get_resolution_strategy")
    void should_get_resolution_strategy(Class<?> inputClass, ResolutionStrategy expectedResult) {
        final AnnotatedType inputType = new AnnotatedType(inputClass);
        assertThat(ModelConversionUtils.getResolutionStrategy(inputType)).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> params_should_get_class_name() {
        return Stream.of(
                Arguments.of("fr.irun.cms.api.model.Page", "Page"),
                Arguments.of("fr.irun.hexamon.api.Entity", "Entity"),
                Arguments.of("fr.irun.hexamon.api.AnyClass", "AnyClass")
        );
    }

    @ParameterizedTest
    @MethodSource("params_should_get_class_name")
    void should_get_class_name(String fullClassName, String classSimpleName) {
        final Type inputType = mock(Type.class);
        when(inputType.getTypeName()).thenReturn("[simple type : " + fullClassName + "]");

        final String actualClassName = ModelConversionUtils.getClassName(inputType);
        assertThat(actualClassName).isNotNull();
        assertThat(actualClassName).endsWith(classSimpleName);
        assertThat(actualClassName).isEqualTo(fullClassName);
    }

    private static Stream<Arguments> params_should_extract_generic_inner_type() {
        final JavaType[] innerTypes = Arrays.array(mock(JavaType.class));

        final ParameterizedType parameterizedType = mock(ParameterizedType.class);
        when(parameterizedType.getActualTypeArguments()).thenReturn(innerTypes);

        final TypeBindings typeBindings = mock(TypeBindings.class);
        when(typeBindings.isEmpty()).thenReturn(false);
        when(typeBindings.getBoundType(0)).thenReturn(innerTypes[0]);
        final TypeBase typeBase = mock(TypeBase.class);
        when(typeBase.getBindings()).thenReturn(typeBindings);

        return Stream.of(
                Arguments.of(new AnnotatedType(parameterizedType), new AnnotatedType(innerTypes[0])),
                Arguments.of(typeBase, innerTypes[0])
        );
    }

    @ParameterizedTest
    @MethodSource("params_should_extract_generic_inner_type")
    void should_extract_generic_inner_type(AnnotatedType genericType, AnnotatedType expectedInnerType) {
        final Optional<AnnotatedType> actual = ModelConversionUtils.extractGenericFirstInnerType(genericType);
        assertThat(actual).isNotNull();
        assertThat(actual).contains(expectedInnerType);
    }

    private static Stream<Arguments> params_should_extract_empty_generic_inner_type() {
        final ParameterizedType parameterizedType = mock(ParameterizedType.class);

        final TypeBindings typeBindings = mock(TypeBindings.class);
        when(typeBindings.isEmpty()).thenReturn(true);
        final TypeBase typeBase = mock(TypeBase.class);
        when(typeBase.getBindings()).thenReturn(typeBindings);

        final Type type = mock(Type.class);

        return Stream.of(
                Arguments.of(new AnnotatedType(parameterizedType)),
                Arguments.of(new AnnotatedType(typeBase)),
                Arguments.of(new AnnotatedType(type)),
                Arguments.of((Type) null)
        );
    }

    @ParameterizedTest
    @MethodSource("params_should_extract_empty_generic_inner_type")
    void should_extract_empty_generic_inner_type(AnnotatedType type) {
        final Optional<AnnotatedType> actual = ModelConversionUtils.extractGenericFirstInnerType(type);
        assertThat(actual).isEmpty();
    }
}
