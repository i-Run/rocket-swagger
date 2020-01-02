package fr.irun.openapi.swagger.resolver;

import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class GenericArrayModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConverter modelConverterMock;
    private ModelConverterContext contextMock;

    private GenericArrayModelResolver tested;

    @BeforeEach
    void setUp() {
        modelConverterMock = mock(ModelConverter.class);
        contextMock = mock(ModelConverterContext.class);

        tested = new GenericArrayModelResolver();
    }

    @Test
    void getModelType() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.WRAP_GENERIC_ARRAY);
    }

    @Test
    void should_resolve_property() {
        final Type innerType = mock(Type.class);
        final ParameterizedType fluxType = mock(ParameterizedType.class);
        final Property expectedProperty = mock(Property.class);

        when(fluxType.getActualTypeArguments()).thenReturn(new Type[]{
                innerType
        });
        when(modelConverterMock.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

        final Iterator<ModelConverter> converterChain = Iterators.forArray(modelConverterMock);
        final Property actualProperty = tested.resolveProperty(fluxType, contextMock, ANNOTATIONS, converterChain);
        assertThat(actualProperty).isNotNull();
        assertThat(actualProperty).isInstanceOf(ArrayProperty.class);
        ArrayProperty actualArrayProperty = (ArrayProperty) actualProperty;
        assertThat(actualArrayProperty.getItems()).isSameAs(expectedProperty);

        verify(modelConverterMock).resolveProperty(same(innerType), same(contextMock), same(ANNOTATIONS), same(converterChain));
        verifyNoMoreInteractions(modelConverterMock);
    }

    @Test
    void should_resolve_model() {
        final Type innerType = mock(Type.class);
        final ParameterizedType fluxType = mock(ParameterizedType.class);
        final Model expectedModel = mock(Model.class);

        when(fluxType.getActualTypeArguments()).thenReturn(new Type[]{
                innerType
        });
        when(modelConverterMock.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Iterator<ModelConverter> converterChain = Iterators.forArray(modelConverterMock);
        final Model actualModel = tested.resolve(fluxType, contextMock, converterChain);
        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);

        verify(modelConverterMock).resolve(same(innerType), same(contextMock), same(converterChain));
        verifyNoMoreInteractions(modelConverterMock);
    }

    @Test
    void should_resolve_null_property_if_no_more_converter() {
        assertThat(tested.resolveProperty(mock(Type.class), contextMock, ANNOTATIONS, Iterators.forArray())).isNull();
    }

    @Test
    void should_resolve_null_model_if_no_more_converter() {
        assertThat(tested.resolve(mock(Type.class), contextMock, Iterators.forArray())).isNull();
    }

    @Test
    void should_resolve_null_property_if_no_inner_type() {
        final Type typeMock = mock(Type.class);
        when(typeMock.getTypeName()).thenReturn("org.springframework.http.ResponseEntity");

        assertThat(tested.resolveProperty(typeMock, contextMock, ANNOTATIONS, Iterators.forArray(modelConverterMock))).isNull();
        verifyZeroInteractions(modelConverterMock);
    }

    @Test
    void should_resolve_null_model_if_no_inner_type() {
        final Type typeMock = mock(Type.class);
        when(typeMock.getTypeName()).thenReturn("org.springframework.http.ResponseEntity");

        assertThat(tested.resolve(typeMock, contextMock, Iterators.forArray(modelConverterMock))).isNull();
        verifyZeroInteractions(modelConverterMock);
    }
}