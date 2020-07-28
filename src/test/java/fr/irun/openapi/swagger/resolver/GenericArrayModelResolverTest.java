package fr.irun.openapi.swagger.resolver;

import com.google.common.collect.Iterators;
import com.google.common.reflect.TypeToken;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("UnstableApiUsage")
class GenericArrayModelResolverTest {
    private ModelConverter modelConverterMock;
    private ModelConverterContext contextMock;

    private GenericArrayModelResolver tested;

    @BeforeEach
    void setUp() {
        modelConverterMock = mock(ModelConverter.class);
        contextMock = mock(ModelConverterContext.class);

        tested = new GenericArrayModelResolver(modelConverterMock);
    }

    @Test
    void getModelType() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.WRAP_GENERIC_ARRAY);
    }

    @Test
    void should_resolve_model() {
        final AnnotatedType innerType = new AnnotatedType(String.class);
        final AnnotatedType fluxType = new AnnotatedType(new TypeToken<Flux<String>>() {
        }.getType());
        final Schema<?> expectedModel = mock(Schema.class);

        when(modelConverterMock.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Iterator<ModelConverter> converterChain = Iterators.forArray();
        final Schema<?> actualModel = tested.resolve(fluxType, contextMock, converterChain);
        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);

        verify(modelConverterMock).resolve(eq(innerType), same(contextMock), same(converterChain));
        verifyNoMoreInteractions(modelConverterMock);
    }

    @Test
    void should_resolve_null_model_if_no_inner_type() {
        final AnnotatedType typeMock = new AnnotatedType(ResponseEntity.class);

        assertThat(tested.resolve(typeMock, contextMock, Iterators.forArray(modelConverterMock))).isNull();
        verifyNoInteractions(modelConverterMock);
    }
}