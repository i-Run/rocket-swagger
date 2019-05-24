package fr.irun.openapi.swagger.converter;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class BaseModelConverterTest {

    private static final Annotation[] DEFAULT_ANNOTATIONS = new Annotation[0];

    private ModelConverter baseModelConverter;
    private ModelConverterContext converterContext;
    private Iterator<ModelConverter> iterator;

    private BaseModelConverter tested;

    @BeforeEach
    void setUp() {
        baseModelConverter = mock(ModelConverter.class);
        converterContext = mock(ModelConverterContext.class);
        iterator = Collections.singletonList(baseModelConverter).iterator();

        tested = new BaseModelConverter(baseModelConverter);
    }

    private static Stream<Arguments> dateTypeArguments() {
        return Stream.of(
                Arguments.of(Instant.class),
                Arguments.of(LocalDateTime.class),
                Arguments.of(java.util.Date.class),
                Arguments.of(java.sql.Date.class)
        );
    }

    @ParameterizedTest
    @MethodSource("dateTypeArguments")
    void resolveProperty_should_resolve_date_property(Type inputType) {
        Property property = tested.resolveProperty(inputType, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(DateTimeProperty.class);
        verifyZeroInteractions(baseModelConverter);
    }

    @Test
    void resolveProperty_should_resolve_non_date_property() {
        final Property expectedOutProperty = mock(Property.class);
        final Type inputType = mock(Type.class);
        when(inputType.getTypeName()).thenReturn(String.class.getName());

        when(baseModelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedOutProperty);

        Property property = tested.resolveProperty(inputType, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isSameAs(expectedOutProperty);

        verify(baseModelConverter).resolveProperty(same(inputType), same(converterContext), same(DEFAULT_ANNOTATIONS), same(iterator));
        verifyZeroInteractions(baseModelConverter);
    }

    @Test
    void resolve() {
        final Model expectedModel = mock(Model.class);
        final Type inputType = mock(Type.class);

        when(baseModelConverter.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Model actualModel = tested.resolve(inputType, converterContext, iterator);
        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);

        verify(baseModelConverter).resolve(same(inputType), same(converterContext), same(iterator));
        verifyNoMoreInteractions(baseModelConverter);
    }

}
