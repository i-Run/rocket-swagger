package fr.irun.openapi.swagger.consolidation;

import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.mock.ParameterizedTypeMock;
import fr.irun.openapi.swagger.mock.TypeMock;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test case for the class {@link PageModelConsolidation}
 */
class PageModelConsolidationTest {

    private static final String INNER_TYPE_NAME = "fr.irun.module.api.model.SomeModel";
    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray(new ModelConverter[0]);
    private static final Annotation[] ANNOTATIONS = new Annotation[0];


    private ModelConverter baseModelConverter;
    private Type baseType;
    private ModelConverterContext context;

    private PageModelConsolidation tested;

    @BeforeEach
    void setUp() {
        baseModelConverter = mock(ModelConverter.class);
        context = mock(ModelConverterContext.class);
        baseType = new ParameterizedTypeMock("fr.irun.hexamon.api.model.Page", new TypeMock(INNER_TYPE_NAME));

        tested = new PageModelConsolidation(baseModelConverter);
        tested.setContext(baseType, context, ANNOTATIONS, ITERATOR);
    }


    @Test
    void getModelType() {
        assertThat(tested.getModelType()).isEqualTo(ModelEnum.PAGE);
    }


    @Test
    void consolidateProperty() {
        {
            final RefProperty inputProperty = mock(RefProperty.class);
            when(inputProperty.get$ref()).thenReturn("My$PageRef");
            when(baseModelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(inputProperty);

            final Property actualProperty = tested.consolidateProperty(inputProperty);
            assertThat(actualProperty).isNotNull();
            assertThat(actualProperty).isSameAs(inputProperty);
            verify(inputProperty).get$ref();
            verify(inputProperty).set$ref(eq("My$HexamonPageRef"));
            verifyNoMoreInteractions(inputProperty);
            verify(baseModelConverter).resolveProperty(same(baseType), same(context), same(ANNOTATIONS), same(ITERATOR));
            verifyNoMoreInteractions(baseModelConverter);
        }
        reset(baseModelConverter);
        {
            final Property inputProperty = mock(Property.class);
            when(baseModelConverter.resolveProperty(any(), any(), any(), any())).thenReturn(inputProperty);

            final Property actualProperty = tested.consolidateProperty(inputProperty);
            assertThat(actualProperty).isNotNull();
            assertThat(actualProperty).isSameAs(inputProperty);
            verifyZeroInteractions(inputProperty);
            verify(baseModelConverter).resolveProperty(same(baseType), same(context), same(ANNOTATIONS), same(ITERATOR));
            verifyNoMoreInteractions(baseModelConverter);
        }
    }

    @Test
    void consolidateModel() {
        {
            final Model inputModel = mock(Model.class);
            when(inputModel.getReference()).thenReturn("MyPageRef");
            when(baseModelConverter.resolve(any(), any(), any())).thenReturn(inputModel);

            final Model actualModel = tested.consolidateModel(inputModel);

            assertThat(actualModel).isNotNull();
            assertThat(actualModel).isSameAs(inputModel);
            verifyZeroInteractions(inputModel);
            verify(baseModelConverter).resolve(same(baseType), same(context), same(ITERATOR));
            verifyNoMoreInteractions(baseModelConverter);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void setContext() {
        final Type baseType = mock(Type.class);
        final ModelConverterContext context = mock(ModelConverterContext.class);
        final Annotation[] annotations = new Annotation[0];
        final Iterator<ModelConverter> converterChain = mock(Iterator.class);

        tested.setContext(baseType, context, annotations, converterChain);

        assertThat(ReflectionUtils.readFieldValue(PageModelConsolidation.class, "baseType", tested)).containsSame(baseType);
        assertThat(ReflectionUtils.readFieldValue(PageModelConsolidation.class, "context", tested)).containsSame(context);
        assertThat(ReflectionUtils.readFieldValue(PageModelConsolidation.class, "annotations", tested)).containsSame(annotations);
        assertThat(ReflectionUtils.readFieldValue(PageModelConsolidation.class, "converterChain", tested)).containsSame(converterChain);
    }
}