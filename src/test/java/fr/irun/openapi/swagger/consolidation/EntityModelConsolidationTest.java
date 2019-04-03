package fr.irun.openapi.swagger.consolidation;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.mock.EntityMock;
import fr.irun.openapi.swagger.mock.PojoMock;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EntityModelConsolidationTest {

    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray(new ModelConverter[0]);
    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private EntityModelConsolidation tested;
    private TypeFactory typeFactory;
    private ModelConverter baseConverter;
    private ModelConverterContext context;
    private RefProperty refProperty;

    private ArgumentCaptor<Type> typeCaptor;


    @BeforeEach
    void setUp() {
        typeCaptor = ArgumentCaptor.forClass(Type.class);

        // Cannot be mocked since final
        typeFactory = TypeFactory.defaultInstance();
        baseConverter = mock(ModelConverter.class);
        context = mock(ModelConverterContext.class);
        refProperty = mock(RefProperty.class);

        tested = new EntityModelConsolidation(typeFactory, baseConverter);
    }

    @Test
    void getModelType() {
        assertThat(tested.getModelType()).isEqualTo(ModelEnum.ENTITY);
    }

    @Test
    void consolidateProperty() {
        when(refProperty.get$ref()).thenReturn("My$Ref");
        Property outProperty = tested.consolidateProperty(refProperty);
        assertThat(outProperty).isNotNull();
        assertThat(outProperty).isSameAs(refProperty);
        verify(refProperty).set$ref(eq("My$RefEntity"));
    }

    @Test
    void consolidatePropertyNonRefProperty() {
        Property inputProperty = new ArrayProperty();

        Property outProperty = tested.consolidateProperty(inputProperty);
        assertThat(outProperty).isNotNull();
        assertThat(outProperty).isSameAs(inputProperty);
    }

    @Test
    void consolidatePropertyNullProperty() {
        Property outProperty = tested.consolidateProperty(null);
        assertThat(outProperty).isNull();
    }

    @Test
    void consolidateModel() {
        final Type parametricType = typeFactory.constructParametricType(EntityMock.class, PojoMock.class);
        tested.setContext(parametricType, context, ANNOTATIONS, ITERATOR);

        when(baseConverter.resolveProperty(any(), any(), any(), any())).thenReturn(refProperty);

        Model inputModel = new RefModel();
        inputModel.setReference("MyModelRef");

        Model outputModel = tested.consolidateModel(inputModel);
        assertThat(outputModel).isNotNull();
        assertThat(outputModel).isInstanceOf(ModelImpl.class);

        ModelImpl modelImpl = (ModelImpl) outputModel;
        assertThat(modelImpl.getReference()).isEqualTo("#/definitions/MyModelRefEntity");
        assertThat(modelImpl.getName()).isEqualTo(PojoMock.class.getSimpleName() + "Entity");

        // Verify the types have been resolved.
        verify(baseConverter, times(9)).resolveProperty(typeCaptor.capture(), same(context), same(ANNOTATIONS), same(ITERATOR));

        assertThat(typeCaptor.getAllValues()).containsExactly(
                String.class, String.class, String.class, Instant.class, Instant.class, // For the EntityMock fields
                int.class, String.class, String.class, Instant.class // For the PojoMock fields
        );

        // Verify the properties have been set
        final Map<String, Property> properties = modelImpl.getProperties();
        assertThat(properties).isNotNull();
        // 5 fields in EntityMock + 4 fields in PojoMock = size 9
        final int expectedPropertiesCount = 9;
        assertThat(properties.size()).isEqualTo(expectedPropertiesCount);
        Property[] expectedValues = new Property[expectedPropertiesCount];
        Arrays.fill(expectedValues, refProperty);
        assertThat(properties).containsValues(expectedValues);
        assertThat(properties).containsKeys(
                "_id", "_createdBy", "_updatedBy", "_createdAt", "_updatedAt",
                "elementId", "name", "description", "validationDate"
        );
    }

    @Test
    void consolidateModelNotGenericType() {
        RefModel inputModel = new RefModel();

        Model outputModel = tested.consolidateModel(inputModel);
        assertThat(outputModel).isNotNull();
        assertThat(outputModel).isSameAs(inputModel);
    }

    @Test
    void consolidateModelNullModel() {
        Model outputModel = tested.consolidateModel(null);
        assertThat(outputModel).isNull();
    }

}
