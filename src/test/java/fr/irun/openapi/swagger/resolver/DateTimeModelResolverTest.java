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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DateTimeModelResolverTest {

    private AnnotatedType typeMock;
    private ModelConverterContext contextMock;
    private ModelConverter modelConverterMock;

    private DateTimeModelResolver tested;

    @BeforeEach
    void setUp() {
        typeMock = mock(AnnotatedType.class);
        contextMock = mock(ModelConverterContext.class);
        modelConverterMock = mock(ModelConverter.class);

        tested = new DateTimeModelResolver();
    }

    @Test
    void should_be_related_to_datetime_strategy() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.DATE_TIME);
    }

    @Test
    void should_resolve_model() {
        final Iterator<ModelConverter> iterator = Iterators.forArray(modelConverterMock);
        final Schema<?> expected = new Schema<>();
        when(modelConverterMock.resolve(typeMock, contextMock, iterator)).thenReturn(expected);

        final Schema<?> actual = tested.resolve(typeMock, contextMock, iterator);
        assertThat(actual).isNotNull();
        assertThat(actual).isSameAs(expected);

        verify(modelConverterMock).resolve(typeMock, contextMock, iterator);
        verifyNoMoreInteractions(modelConverterMock);
        verifyNoInteractions(typeMock, contextMock);
    }

    @Test
    void should_resolve_null_model_if_no_more_converter() {
        assertThat(tested.resolve(typeMock, contextMock, Iterators.forArray())).isNull();
    }
}