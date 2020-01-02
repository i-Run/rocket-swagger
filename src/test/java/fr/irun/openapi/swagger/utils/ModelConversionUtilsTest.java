package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelConversionUtilsTest {

    @Test
    void isResponseEntityType() {
        {
            final Type inputType = mockType("org.springframework.http.ResponseEntity");
            assertThat(ModelConversionUtils.isResponseEntityType(inputType)).isTrue();
        }
        {
            final Type inputType = mockType("org.springframework.http.ResponseEntity<java.lang.Integer>");
            assertThat(ModelConversionUtils.isResponseEntityType(inputType)).isTrue();
        }
        assertThat(ModelConversionUtils.isResponseEntityType(String.class)).isFalse();
        assertThat(ModelConversionUtils.isResponseEntityType(null)).isFalse();
    }


    private static Stream<Arguments> computeModelTypeParams() {
        return Stream.of(
                Arguments.of(Mono.class, ResolutionStrategy.WRAP_GENERIC),
                Arguments.of(Flux.class, ResolutionStrategy.WRAP_GENERIC_ARRAY),
                Arguments.of(String.class, ResolutionStrategy.DEFAULT),
                Arguments.of(Instant.class, ResolutionStrategy.DATE_TIME),
                Arguments.of(LocalDateTime.class, ResolutionStrategy.DATE_TIME),
                Arguments.of(java.util.Date.class, ResolutionStrategy.DATE_TIME),
                Arguments.of(java.sql.Date.class, ResolutionStrategy.DATE_TIME)
        );
    }


    @ParameterizedTest
    @MethodSource("computeModelTypeParams")
    void computeModelType(Class<?> inputClass, ResolutionStrategy expectedResult) {
        final Type inputType = mockType(inputClass);
        assertThat(ModelConversionUtils.computeModelType(inputType)).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> classNameParams() {
        return Stream.of(
                Arguments.of("fr.irun.cms.api.model.Page", "Page"),
                Arguments.of("fr.irun.hexamon.api.Entity", "Entity"),
                Arguments.of("fr.irun.hexamon.api.AnyClass", "AnyClass")
        );
    }

    @ParameterizedTest
    @MethodSource("classNameParams")
    void getClassName(String fullClassName, String classSimpleName) {
        final Type inputType = mockType(fullClassName);
        final String actualClassName = ModelConversionUtils.getClassName(inputType);
        assertThat(actualClassName).isNotNull();
        assertThat(actualClassName).endsWith(classSimpleName);
        assertThat(actualClassName).isEqualTo(fullClassName);

    }

    private static Type mockType(Object classValue) {
        final Type mockType = mock(Type.class);
        when(mockType.getTypeName()).thenReturn("[simple type : " + classValue + "]");
        return mockType;
    }

    @Test
    void extractGenericFirstInnerType() {
        // Parameterized type
        {
            final Type expectedType = mock(Type.class);
            final ParameterizedType inputType = mock(ParameterizedType.class);
            when(inputType.getActualTypeArguments()).thenReturn(new Type[]{expectedType});

            final Type actualType = ModelConversionUtils.extractGenericFirstInnerType(inputType);
            assertThat(actualType).isNotNull();
            assertThat(actualType).isSameAs(expectedType);
        }
        // TypeBase
        {
            final JavaType expectedType = mock(JavaType.class);
            final TypeBindings bindings = mock(TypeBindings.class);
            when(bindings.isEmpty()).thenReturn(false);
            when(bindings.getBoundType(eq(0))).thenReturn(expectedType);
            final TypeBase inputType = mock(TypeBase.class);
            when(inputType.getBindings()).thenReturn(bindings);

            final Type actualType = ModelConversionUtils.extractGenericFirstInnerType(inputType);
            assertThat(actualType).isNotNull();
            assertThat(actualType).isSameAs(expectedType);
        }
        // Empty Parameterized type
        {
            final ParameterizedType inputType = mock(ParameterizedType.class);

            final Type actualType = ModelConversionUtils.extractGenericFirstInnerType(inputType);
            assertThat(actualType).isNull();
        }
        // Empty TypeBase
        {
            final TypeBindings bindings = mock(TypeBindings.class);
            when(bindings.isEmpty()).thenReturn(true);
            final TypeBase inputType = mock(TypeBase.class);
            when(inputType.getBindings()).thenReturn(bindings);

            final Type actualType = ModelConversionUtils.extractGenericFirstInnerType(inputType);
            assertThat(actualType).isNull();
        }
        // Non-generic type
        {
            final Type inputType = mock(Type.class);

            final Type actualType = ModelConversionUtils.extractGenericFirstInnerType(inputType);
            assertThat(actualType).isNull();
        }
        // Null type
        {
            final Type actualType = ModelConversionUtils.extractGenericFirstInnerType(null);
            assertThat(actualType).isNull();
        }
    }
}
