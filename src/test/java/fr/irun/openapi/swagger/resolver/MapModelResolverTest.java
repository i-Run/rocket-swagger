package fr.irun.openapi.swagger.resolver;

import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MapModelResolverTest {

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
    void should_resolve_map_model() {
        final Iterator<ModelConverter> converterChain = Iterators.forArray(converterMock);
        final Schema<?> expected = mock(Schema.class);
        AnnotatedType type = spy(new AnnotatedType(Map.class));
        when(converterMock.resolve(eq(new AnnotatedType(Map.class)), same(contextMock), same(converterChain))).thenReturn(expected);

        final Schema<?> actual = tested.resolve(type, contextMock, converterChain);

        assertThat(actual).isNotNull();
        assertThat(actual).isSameAs(expected);

        verify(converterMock).resolve(eq(new AnnotatedType(Map.class)), same(contextMock), same(converterChain));
        verifyNoMoreInteractions(converterMock);
        verifyNoInteractions(contextMock, type);
    }

    @Test
    void should_resolve_null_model_if_no_more_converters() {
        assertThat(tested.resolve(new AnnotatedType(Map.class), contextMock, Iterators.forArray())).isNull();
    }
}