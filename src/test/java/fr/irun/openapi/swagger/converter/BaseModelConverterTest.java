package fr.irun.openapi.swagger.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BaseModelConverterTest {

    private static final String RESPONSE_ENTITY_CLASS_NAME = "org.springframework.http.ResponseEntity";

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
    void should_resolve_ResponseEntity_property() {
        final JavaType innerType = TypeFactory.defaultInstance().constructType(Integer.class);

        final ParameterizedType baseType = mock(ParameterizedType.class);
        when(baseType.getTypeName()).thenReturn(RESPONSE_ENTITY_CLASS_NAME);
        when(baseType.getActualTypeArguments()).thenReturn(new Type[]{innerType});

        final Property expectedOutProperty = mock(Property.class);
        when(baseModelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedOutProperty);

        Property actualProperty = tested.resolveProperty(baseType, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(actualProperty).isNotNull();
        assertThat(actualProperty).isSameAs(expectedOutProperty);

        verify(baseModelConverter).resolveProperty(same(innerType), same(converterContext), same(DEFAULT_ANNOTATIONS), same(iterator));
        verifyNoMoreInteractions(baseModelConverter);
    }

    @Test
    void should_resolve_any_other_type_property() {
        final Property expectedOutProperty = mock(Property.class);
        final Type inputType = mock(Type.class);
        when(inputType.getTypeName()).thenReturn(String.class.getName());

        when(baseModelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedOutProperty);

        Property property = tested.resolveProperty(inputType, converterContext, DEFAULT_ANNOTATIONS, iterator);

        assertThat(property).isNotNull();
        assertThat(property).isSameAs(expectedOutProperty);

        verify(baseModelConverter).resolveProperty(same(inputType), same(converterContext), same(DEFAULT_ANNOTATIONS), same(iterator));
        verifyNoMoreInteractions(baseModelConverter);
    }

    @Test
    void should_resolve_ResponseEntity_model() {
        final JavaType innerType = TypeFactory.defaultInstance().constructType(Integer.class);

        final ParameterizedType baseType = mock(ParameterizedType.class);
        when(baseType.getTypeName()).thenReturn(RESPONSE_ENTITY_CLASS_NAME);
        when(baseType.getActualTypeArguments()).thenReturn(new Type[]{innerType});

        final Model expectedOutModel = mock(Model.class);
        when(baseModelConverter.resolve(any(), any(), any())).thenReturn(expectedOutModel);

        Model actualModel = tested.resolve(baseType, converterContext, iterator);

        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedOutModel);

        verify(baseModelConverter).resolve(same(innerType), same(converterContext), same(iterator));
        verifyNoMoreInteractions(baseModelConverter);
    }

    @Test
    void should_resolve_any_other_type_model() {
        {
            final Model expectedModel = mock(Model.class);
            final Type inputType = TypeFactory.defaultInstance().constructType(Instant.class);

            when(baseModelConverter.resolve(any(), any(), any())).thenReturn(expectedModel);

            final Model actualModel = tested.resolve(inputType, converterContext, iterator);
            assertThat(actualModel).isNotNull();
            assertThat(actualModel).isSameAs(expectedModel);

            verify(baseModelConverter).resolve(same(inputType), same(converterContext), same(iterator));
            verifyNoMoreInteractions(baseModelConverter);
        }
        reset(baseModelConverter);
        {
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

}
