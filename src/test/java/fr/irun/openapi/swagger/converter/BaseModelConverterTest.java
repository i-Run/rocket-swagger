package fr.irun.openapi.swagger.converter;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void resolvePropertyInstant() {
        Property property = tested.resolveProperty(Instant.class, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(DateTimeProperty.class);
        verifyZeroInteractions(baseModelConverter);
    }

    @Test
    void resolvePropertyLocalDateTime() {
        Property property = tested.resolveProperty(LocalDateTime.class, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(DateTimeProperty.class);
        verifyZeroInteractions(baseModelConverter);
    }

    @Test
    void resolvePropertyNonDate() {
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
    void resolvePropertyJsonNode() {
        Property property = tested.resolveProperty(JsonNode.class, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(MapProperty.class);
        verifyZeroInteractions(baseModelConverter);
    }

    @Test
    void resolveNullProperty() {
        Model expectedOutModel = new ModelImpl();
        when(baseModelConverter.resolve(any(), any(), any())).thenReturn(expectedOutModel);

        Model model = tested.resolve(String.class, converterContext, iterator);

        assertThat(model).isNotNull();
        assertThat(model).isSameAs(expectedOutModel);

        verify(baseModelConverter).resolve(eq(String.class), eq(converterContext), eq(iterator));
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
