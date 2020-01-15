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
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class MapModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConverter converterMock;
    private ModelConverterContext contextMock;

    private MapModelResolver tested;

    @BeforeEach
    void setUp() {
        converterMock = mock(ModelConverter.class);
        contextMock = mock(ModelConverterContext.class);

        tested = new MapModelResolver();
    }

    @Test
    void should_have_map_resolution_strategy() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.MAP);
    }

    @Test
    void should_resolve_map_property() {
        final Iterator<ModelConverter> converterChain = Iterators.forArray(converterMock);
        final Property expected = mock(Property.class);
        when(converterMock.resolveProperty(Map.class, contextMock, ANNOTATIONS, converterChain)).thenReturn(expected);

        final Type inputType = mock(Type.class);
        final Property actual = tested.resolveProperty(inputType, contextMock, ANNOTATIONS, converterChain);

        assertThat(actual).isNotNull();
        assertThat(actual).isSameAs(expected);

        verify(converterMock).resolveProperty(Map.class, contextMock, ANNOTATIONS, converterChain);
        verifyNoMoreInteractions(converterMock);
        verifyZeroInteractions(contextMock, inputType);
    }

    @Test
    void should_resolve_map_model() {
        final Iterator<ModelConverter> converterChain = Iterators.forArray(converterMock);
        final Model expected = mock(Model.class);
        when(converterMock.resolve(Map.class, contextMock, converterChain)).thenReturn(expected);

        final Type inputType = mock(Type.class);
        final Model actual = tested.resolve(inputType, contextMock, converterChain);

        assertThat(actual).isNotNull();
        assertThat(actual).isSameAs(expected);

        verify(converterMock).resolve(Map.class, contextMock, converterChain);
        verifyNoMoreInteractions(converterMock);
        verifyZeroInteractions(contextMock, inputType);
    }

    @Test
    void should_resolve_null_property_if_no_more_converters() {
        assertThat(tested.resolveProperty(mock(Type.class), contextMock, ANNOTATIONS, Iterators.forArray())).isNull();
    }

    @Test
    void should_resolve_null_model_if_no_more_converters() {
        assertThat(tested.resolve(mock(Type.class), contextMock, Iterators.forArray())).isNull();
    }
}