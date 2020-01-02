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
import static org.mockito.Mockito.when;

class GenericArrayModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConverter modelConverter;
    private ModelConverterContext context;

    private GenericArrayModelResolver tested;

    @BeforeEach
    void setUp() {
        modelConverter = mock(ModelConverter.class);
        context = mock(ModelConverterContext.class);

        tested = new GenericArrayModelResolver();
    }

    @Test
    void getModelType() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.WRAP_GENERIC_ARRAY);
    }

    @Test
    void should_resolve_property() {
        final Type innerFluxType = mock(Type.class);
        final ParameterizedType fluxType = mock(ParameterizedType.class);
        final Property expectedProperty = mock(Property.class);

        when(fluxType.getActualTypeArguments()).thenReturn(new Type[]{
                innerFluxType
        });
        when(modelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

        final Iterator<ModelConverter> converterChain = Iterators.forArray(modelConverter);
        final Property actualProperty = tested.resolveProperty(fluxType, context, ANNOTATIONS, converterChain);
        assertThat(actualProperty).isNotNull();
        assertThat(actualProperty).isInstanceOf(ArrayProperty.class);
        ArrayProperty actualArrayProperty = (ArrayProperty) actualProperty;
        assertThat(actualArrayProperty.getItems()).isSameAs(expectedProperty);

        verify(modelConverter).resolveProperty(same(innerFluxType), same(context), same(ANNOTATIONS), same(converterChain));
        verifyNoMoreInteractions(modelConverter);
    }

    @Test
    void should_resolve_model() {
        final Type innerFluxType = mock(Type.class);
        final ParameterizedType fluxType = mock(ParameterizedType.class);
        final Model expectedModel = mock(Model.class);

        when(fluxType.getActualTypeArguments()).thenReturn(new Type[]{
                innerFluxType
        });
        when(modelConverter.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Iterator<ModelConverter> converterChain = Iterators.forArray(modelConverter);
        final Model actualModel = tested.resolve(fluxType, context, converterChain);
        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);

        verify(modelConverter).resolve(same(innerFluxType), same(context), same(converterChain));
        verifyNoMoreInteractions(modelConverter);
    }

    @Test
    void should_resolve_null_property_if_no_more_converter() {
        assertThat(tested.resolveProperty(mock(Type.class), context, ANNOTATIONS, Iterators.forArray())).isNull();
    }

    @Test
    void should_resolve_null_model_if_no_more_converter() {
        assertThat(tested.resolve(mock(Type.class), context, Iterators.forArray())).isNull();
    }
}