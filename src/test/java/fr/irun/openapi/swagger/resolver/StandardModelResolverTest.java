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

class StandardModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConverter modelConverter;
    private ModelConverterContext context;

    private StandardModelResolver tested;

    @BeforeEach
    void setUp() {
        modelConverter = mock(ModelConverter.class);
        context = mock(ModelConverterContext.class);

        tested = new StandardModelResolver(modelConverter);
    }

    @Test
    void getModelType() {
        assertThat(tested.getResolutionStrategy()).isEqualTo(ResolutionStrategy.DEFAULT);
    }

    @Test
    void resolveProperty() {
        final Type baseType = mock(Type.class);
        final Property expectedProperty = mock(Property.class);
        final Iterator<ModelConverter> iterator = Iterators.forArray();
        when(modelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

        final Property actualProperty = tested.resolveProperty(baseType, context, ANNOTATIONS, iterator);

        assertThat(actualProperty).isNotNull();
        assertThat(actualProperty).isSameAs(expectedProperty);
        verify(modelConverter).resolveProperty(same(baseType), same(context), same(ANNOTATIONS), same(iterator));
        verifyNoMoreInteractions(modelConverter);

    }

    @Test
    void resolve() {
        final Type baseType = mock(Type.class);
        final Model expectedModel = mock(Model.class);
        final Iterator<ModelConverter> iterator = Iterators.forArray();
        when(modelConverter.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Model actualModel = tested.resolve(baseType, context, iterator);

        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);
        verify(modelConverter).resolve(same(baseType), same(context), same(iterator));
        verifyNoMoreInteractions(modelConverter);
    }
}