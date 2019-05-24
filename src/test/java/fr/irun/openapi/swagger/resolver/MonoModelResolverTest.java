package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelEnum;
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
import static org.mockito.Mockito.when;

class MonoModelResolverTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConverter modelConverter;
    private ModelConverterContext context;
    private Iterator<ModelConverter> converterChain;

    private MonoModelResolver tested;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        modelConverter = mock(ModelConverter.class);
        context = mock(ModelConverterContext.class);
        converterChain = mock(Iterator.class);

        tested = new MonoModelResolver(modelConverter);
    }

    @Test
    void getModelType() {
        assertThat(tested.getModelType()).isEqualTo(ModelEnum.MONO);
    }

    @Test
    void resolveProperty() {
        final Type innerMonoType = mock(Type.class);
        final ParameterizedType monoType = mock(ParameterizedType.class);
        final Property expectedProperty = mock(Property.class);

        when(monoType.getActualTypeArguments()).thenReturn(new Type[]{
                innerMonoType
        });
        when(modelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

        final Property actualProperty = tested.resolveProperty(monoType, context, ANNOTATIONS, converterChain);
        assertThat(actualProperty).isNotNull();
        assertThat(actualProperty).isSameAs(expectedProperty);

        verify(modelConverter).resolveProperty(same(innerMonoType), same(context), same(ANNOTATIONS), same(converterChain));
        verifyNoMoreInteractions(modelConverter);
    }

    @Test
    void resolve() {
        final Type innerMonoType = mock(Type.class);
        final ParameterizedType monoType = mock(ParameterizedType.class);
        final Model expectedModel = mock(Model.class);

        when(monoType.getActualTypeArguments()).thenReturn(new Type[]{
                innerMonoType
        });
        when(modelConverter.resolve(any(), any(), any())).thenReturn(expectedModel);

        final Model actualModel = tested.resolve(monoType, context, converterChain);
        assertThat(actualModel).isNotNull();
        assertThat(actualModel).isSameAs(expectedModel);

        verify(modelConverter).resolve(same(innerMonoType), same(context), same(converterChain));
        verifyNoMoreInteractions(modelConverter);
    }
}