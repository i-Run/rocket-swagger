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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DefaultModelResolverTest {

    private ModelConverter converterMock;
    private ModelConverterContext contextMock;

    private DefaultModelResolver tested;

    @BeforeEach
    void setUp() {
        converterMock = mock(ModelConverter.class);
        contextMock = mock(ModelConverterContext.class);

        tested = new DefaultModelResolver();
    }

    @Test
    void getModelType() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.DEFAULT);
    }

    @Test
    void should_resolve_model() {
        final AnnotatedType baseType = mock(AnnotatedType.class);
        final Schema<?> expectedModel = mock(Schema.class);
        final Iterator<ModelConverter> iterator = Iterators.forArray(converterMock);
        when(converterMock.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Schema<?> actualModel = tested.resolve(baseType, contextMock, iterator);

        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);
        verify(converterMock).resolve(same(baseType), same(contextMock), same(iterator));
        verifyNoMoreInteractions(converterMock);
    }

    @Test
    void should_resolve_null_model_if_no_more_converter() {
        assertThat(tested.resolve(mock(AnnotatedType.class), contextMock, Iterators.forArray())).isNull();
    }
}