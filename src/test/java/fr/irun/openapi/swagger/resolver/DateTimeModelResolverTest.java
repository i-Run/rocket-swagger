package fr.irun.openapi.swagger.resolver;


import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class DateTimeModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private Type typeMock;
    private ModelConverterContext contextMock;
    private ModelConverter modelConverterMock;

    private DateTimeModelResolver tested;

    @BeforeEach
    void setUp() {
        typeMock = mock(Type.class);
        contextMock = mock(ModelConverterContext.class);
        modelConverterMock = mock(ModelConverter.class);

        tested = new DateTimeModelResolver();
    }

    @Test
    void should_be_related_to_datetime_strategy() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.DATE_TIME);
    }

    @Test
    void should_resolve_property_as_date_time() {
        final Iterator<ModelConverter> iterator = Iterators.forArray(modelConverterMock);

        final Property actual = tested.resolveProperty(typeMock, contextMock, ANNOTATIONS, iterator);

        assertThat(actual).isNotNull();
        assertThat(actual).isInstanceOf(DateTimeProperty.class);

        verifyZeroInteractions(typeMock, contextMock, modelConverterMock);
    }

    @Test
    void should_resolve_model() {
        final Iterator<ModelConverter> iterator = Iterators.forArray(modelConverterMock);
        final Model expected = new ModelImpl();
        when(modelConverterMock.resolve(typeMock, contextMock, iterator)).thenReturn(expected);

        final Model actual = tested.resolve(typeMock, contextMock, iterator);
        assertThat(actual).isNotNull();
        assertThat(actual).isSameAs(expected);

        verify(modelConverterMock).resolve(typeMock, contextMock, iterator);
        verifyNoMoreInteractions(modelConverterMock);
        verifyZeroInteractions(typeMock, contextMock);
    }

    @Test
    void should_resolve_null_model_if_no_more_converter() {
        assertThat(tested.resolve(typeMock, contextMock, Iterators.forArray())).isNull();
    }
}