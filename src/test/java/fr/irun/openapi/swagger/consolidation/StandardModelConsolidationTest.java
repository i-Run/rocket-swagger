package fr.irun.openapi.swagger.consolidation;

import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StandardModelConsolidationTest {

    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray(new ModelConverter[0]);
    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConverter baseConverter;
    private ModelConverterContext context;
    private Type type;

    private StandardModelConsolidation tested;

    @BeforeEach
    void setUp() {
        baseConverter = mock(ModelConverter.class);
        context = mock(ModelConverterContext.class);
        type = mock(Type.class);

        tested = new StandardModelConsolidation(baseConverter);
    }

    @Test
    void getModelType() {
        assertThat(tested.getModelType()).isEqualTo(ModelEnum.STANDARD);
    }

    @Test
    void consolidateProperty() {
        tested.setContext(type, context, ANNOTATIONS, ITERATOR);

        RefProperty expectedOutProperty = new RefProperty();
        when(baseConverter.resolveProperty(any(), any(), any(), any())).thenReturn(expectedOutProperty);

        // Standard is expected to accept null properties.
        Property property = tested.consolidateProperty(null);
        assertThat(property).isNotNull();
        verify(baseConverter).resolveProperty(same(type), same(context), same(ANNOTATIONS), same(ITERATOR));
        assertThat(property).isSameAs(expectedOutProperty);
    }

    @Test
    void consolidatePropertyNullProperty() {
        Property outProperty = tested.consolidateProperty(null);
        assertThat(outProperty).isNull();
    }

    @Test
    void consolidateModel() {
        tested.setContext(type, context, ANNOTATIONS, ITERATOR);
        RefModel expectedOutModel = new RefModel();
        when(baseConverter.resolve(any(), any(), any())).thenReturn(expectedOutModel);

        // Standard is expected to accept null models.
        Model model = tested.consolidateModel(null);
        assertThat(model).isNotNull();
        verify(baseConverter).resolve(same(type), same(context), same(ITERATOR));
        assertThat(model).isSameAs(expectedOutModel);
    }

    @Test
    void consolidateModelNullModel() {
        Model outputModel = tested.consolidateModel(null);
        assertThat(outputModel).isNull();
    }

}