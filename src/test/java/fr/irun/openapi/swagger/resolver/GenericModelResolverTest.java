package fr.irun.openapi.swagger.resolver;

import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
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

class GenericModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConverter modelConverterMock;
    private ModelConverterContext contextMock;

    private GenericModelResolver tested;

    @BeforeEach
    void setUp() {
        modelConverterMock = mock(ModelConverter.class);
        contextMock = mock(ModelConverterContext.class);

        tested = new GenericModelResolver(modelConverterMock);
    }

    @Test
    void getModelType() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.WRAP_GENERIC);
    }

    @Test
    void should_resolve_property() {
        final Type innerType = mock(Type.class);
        final ParameterizedType monoType = mock(ParameterizedType.class);
        final Property expectedProperty = mock(Property.class);

        when(monoType.getActualTypeArguments()).thenReturn(new Type[]{
                innerType
        });
        when(modelConverterMock.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

        final Iterator<ModelConverter> converterChain = Iterators.forArray();
        final Property actualProperty = tested.resolveProperty(monoType, contextMock, ANNOTATIONS, converterChain);
        assertThat(actualProperty).isNotNull();
        assertThat(actualProperty).isSameAs(expectedProperty);

        verify(modelConverterMock).resolveProperty(same(innerType), same(contextMock), same(ANNOTATIONS), same(converterChain));
        verifyNoMoreInteractions(modelConverterMock);
    }

    @Test
    void should_resolve_model() {
        final Type innerType = mock(Type.class);
        final ParameterizedType monoType = mock(ParameterizedType.class);
        final Model expectedModel = mock(Model.class);

        when(monoType.getActualTypeArguments()).thenReturn(new Type[]{
                innerType
        });
        when(modelConverterMock.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Iterator<ModelConverter> converterChain = Iterators.forArray();
        final Model actualModel = tested.resolve(monoType, contextMock, converterChain);
        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);

        verify(modelConverterMock).resolve(same(innerType), same(contextMock), same(converterChain));
        verifyNoMoreInteractions(modelConverterMock);
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