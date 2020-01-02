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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DefaultModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

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
    void should_resolve_property() {
        final Type baseType = mock(Type.class);
        final Property expectedProperty = mock(Property.class);
        final Iterator<ModelConverter> iterator = Iterators.forArray(converterMock);
        when(converterMock.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

        final Property actualProperty = tested.resolveProperty(baseType, contextMock, ANNOTATIONS, iterator);

        assertThat(actualProperty).isNotNull();
        assertThat(actualProperty).isSameAs(expectedProperty);
        verify(converterMock).resolveProperty(same(baseType), same(contextMock), same(ANNOTATIONS), same(iterator));
        verifyNoMoreInteractions(converterMock);

    }

    @Test
    void should_resolve_model() {
        final Type baseType = mock(Type.class);
        final Model expectedModel = mock(Model.class);
        final Iterator<ModelConverter> iterator = Iterators.forArray(converterMock);
        when(converterMock.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Model actualModel = tested.resolve(baseType, contextMock, iterator);

        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);
        verify(converterMock).resolve(same(baseType), same(contextMock), same(iterator));
        verifyNoMoreInteractions(converterMock);
    }

    @Test
    void should_resolve_null_property_if_no_more_converter() {
        assertThat(tested.resolveProperty(mock(Type.class), contextMock, ANNOTATIONS, Iterators.forArray())).isNull();
    }

    @Test
    void should_resolve_model_if_no_more_converter() {
        assertThat(tested.resolve(mock(Type.class), contextMock, Iterators.forArray())).isNull();
    }
}