package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.mock.EntityMock;
import fr.irun.openapi.swagger.mock.PojoMock;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HexamonEntityModelConverterTest {

    private static final Annotation[] ANNOTATIONS = new Annotation[0];
    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray(new ModelConverter[0]);
    private static final String ENTITY_SUFFIX = "Entity";

    private ModelConverter baseConverter;
    private TypeFactory typeFactory;
    private RefProperty refProperty;

    private Model baseModel;
    private ModelConverterContext context;
    private Property baseProperty;

    private ArgumentCaptor<Type> typeCaptor;

    private HexamonEntityModelConverter tested;

    @BeforeEach
    void setUp() {
        refProperty = mock(RefProperty.class);
        baseModel = mock(Model.class);
        context = mock(ModelConverterContext.class);
        baseProperty = mock(Property.class);
        typeCaptor = ArgumentCaptor.forClass(Type.class);

        baseConverter = mock(ModelConverter.class);

        // Cannot be mocked since final
        typeFactory = TypeFactory.defaultInstance();
        tested = new HexamonEntityModelConverter(baseConverter, typeFactory);
    }


    @Test
    void resolveProperty() {
        final Type entityType = typeFactory.constructParametricType(EntityMock.class, PojoMock.class);

        final String basePropertyRef = "#/definitions/SomePojo";

        when(refProperty.get$ref()).thenReturn(basePropertyRef);
        when(baseConverter.resolveProperty(any(), any(), any(), any())).thenReturn(refProperty);

        Property property = tested.resolveProperty(entityType, context, ANNOTATIONS, ITERATOR);
        assertThat(property).isNotNull();
        assertThat(property).isSameAs(refProperty);

        // Check the suffix has been added to the property.
        verify(refProperty).set$ref(eq(basePropertyRef + ENTITY_SUFFIX));
        verify(baseConverter).resolveProperty(typeCaptor.capture(), same(context), same(ANNOTATIONS), same(ITERATOR));

        final Type realType = typeCaptor.getValue();
        assertThat(realType).isNotNull();
        assertThat(realType).isInstanceOf(JavaType.class);
        assertThat(((JavaType) realType).getRawClass()).isEqualTo(PojoMock.class);
    }


    @Test
    void resolvePropertyNonRefProperty() {
        final Type entityType = typeFactory.constructParametricType(EntityMock.class, PojoMock.class);

        when(baseConverter.resolveProperty(any(), any(), any(), any())).thenReturn(baseProperty);

        Property property = tested.resolveProperty(entityType, context, ANNOTATIONS, ITERATOR);
        assertThat(property).isNotNull();
        assertThat(property).isSameAs(baseProperty);

        verify(baseConverter).resolveProperty(typeCaptor.capture(), same(context), same(ANNOTATIONS), same(ITERATOR));
        final Type realType = typeCaptor.getValue();
        assertThat(realType).isNotNull();
        assertThat(realType).isInstanceOf(JavaType.class);
        assertThat(((JavaType) realType).getRawClass()).isEqualTo(PojoMock.class);
    }


    @Test
    void resolvePropertyNonGeneric() {
        when(baseConverter.resolveProperty(any(), any(), any(), any())).thenReturn(refProperty);

        Property property = tested.resolveProperty(String.class, context, ANNOTATIONS, ITERATOR);
        assertThat(property).isNotNull();
        assertThat(property).isSameAs(refProperty);

        verify(baseConverter).resolveProperty(eq(String.class), same(context), same(ANNOTATIONS), same(ITERATOR));
    }

    @Test
    void resolve() {
        when(baseModel.getDescription()).thenReturn("Some description");
        when(baseModel.getReference()).thenReturn("Some reference");
        when(baseModel.getTitle()).thenReturn("Some title");
        final ExternalDocs externalDocs = new ExternalDocs();
        when(baseModel.getExternalDocs()).thenReturn(externalDocs);
        when(baseModel.getExample()).thenReturn("Some example");

        when(baseConverter.resolve(any(), any(), any())).thenReturn(baseModel);
        when(baseConverter.resolveProperty(any(), any(), any(), any())).thenReturn(baseProperty);

        final Type entityType = typeFactory.constructParametricType(EntityMock.class, PojoMock.class);
        Model model = tested.resolve(entityType, context, ITERATOR);

        // Check the result
        assertThat(model).isNotNull();
        assertThat(model.getDescription()).isEqualTo("Some description");
        assertThat(model.getReference()).isEqualTo("Some reference" + ENTITY_SUFFIX);
        assertThat(model.getTitle()).isEqualTo("Some title");
        assertThat(model.getExternalDocs()).isSameAs(externalDocs);
        assertThat(model.getExample()).isEqualTo("Some example");
        assertThat(model).isInstanceOf(ModelImpl.class);
        assertThat(((ModelImpl) model).getName()).isEqualTo(PojoMock.class.getSimpleName() + ENTITY_SUFFIX);

        // Verify the correct type has been converted.
        verify(baseConverter).resolve(typeCaptor.capture(), same(context), same(ITERATOR));
        Type convertedType = typeCaptor.getValue();
        assertThat(convertedType).isInstanceOf(JavaType.class);
        assertThat(((JavaType) convertedType).getRawClass()).isEqualTo(PojoMock.class);

        // Verify the properties have been set
        final Map<String, Property> properties = model.getProperties();
        assertThat(properties).isNotNull();
        // 5 fields in EntityMock + 4 fields in PojoMock = size 9
        final int expectedPropertiesCount = 9;
        assertThat(properties.size()).isEqualTo(expectedPropertiesCount);
        Property[] expectedValues = new Property[expectedPropertiesCount];
        Arrays.fill(expectedValues, baseProperty);
        assertThat(properties).containsValues(expectedValues);
        assertThat(properties).containsKeys(
                "_id", "_createdBy", "_updatedBy", "_createdAt", "_updatedAt",
                "elementId", "name", "description", "validationDate"
        );

    }

    @Test
    void resolveNonGenericType() {
        when(baseConverter.resolve(any(), any(), any())).thenReturn(baseModel);

        Model model = tested.resolve(String.class, context, ITERATOR);
        assertThat(model).isNotNull();
        assertThat(model).isSameAs(baseModel);

        verify(baseConverter).resolve(eq(String.class), same(context), same(ITERATOR));
    }

}
