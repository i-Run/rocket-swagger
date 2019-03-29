package fr.irun.openapi.swagger;

import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.utils.PropertyModelConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DateTimeModelConverterTest {

    private static final Annotation[] DEFAULT_ANNOTATIONS = new Annotation[0];

    private PropertyModelConverter propertyModelConverter;
    private ModelConverter baseModelConverter;
    private ModelConverterContext converterContext;
    private Iterator<ModelConverter> iterator;

    private DateTimeModelConverter tested;

    @BeforeEach
    void setUp() {
        baseModelConverter = mock(ModelConverter.class);
        propertyModelConverter = mock(PropertyModelConverter.class);
        converterContext = mock(ModelConverterContext.class);
        iterator = Collections.singletonList(baseModelConverter).iterator();

        tested = new DateTimeModelConverter(propertyModelConverter, baseModelConverter);
    }

    @Test
    void resolvePropertyInstant() {
        Property property = tested.resolveProperty(Instant.class, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(DateTimeProperty.class);
        verify(baseModelConverter, never()).resolveProperty(any(), any(), any(), any());
    }

    @Test
    void resolvePropertyLocalDateTime() {
        Property property = tested.resolveProperty(LocalDateTime.class, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(DateTimeProperty.class);
        verify(baseModelConverter, never()).resolveProperty(any(), any(), any(), any());
    }

    @Test
    void resolvePropertyNonDate() {
        Property expectedOutProperty = new RefProperty();
        when(baseModelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedOutProperty);

        Property property = tested.resolveProperty(String.class, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isSameAs(expectedOutProperty);
        verify(baseModelConverter).resolveProperty(eq(String.class), eq(converterContext), eq(DEFAULT_ANNOTATIONS), eq(iterator));
    }

    @Test
    void resolveNullProperty() {
        Model expectedOutModel = new ModelImpl();
        when(baseModelConverter.resolve(any(), any(), any())).thenReturn(expectedOutModel);

        Model model = tested.resolve(String.class, converterContext, iterator);

        assertThat(model).isNotNull();
        assertThat(model).isSameAs(expectedOutModel);

        verify(propertyModelConverter, never()).propertyToModel(any());
        verify(baseModelConverter).resolve(eq(String.class), eq(converterContext), eq(iterator));
    }

    @Test
    void resolve() {
        Property expectedOutProperty = new RefProperty();
        Model expectedOutModel = new ModelImpl();

        when(baseModelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedOutProperty);
        when(propertyModelConverter.propertyToModel(any())).thenReturn(expectedOutModel);

        Model model = tested.resolve(String.class, converterContext, iterator);

        assertThat(model).isNotNull();
        assertThat(model).isSameAs(expectedOutModel);

        verify(propertyModelConverter).propertyToModel(same(expectedOutProperty));
        verify(baseModelConverter, never()).resolve(any(), any(), any());
    }

}
