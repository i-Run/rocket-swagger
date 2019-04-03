package fr.irun.openapi.swagger;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.consolidation.ModelConsolidation;
import fr.irun.openapi.swagger.exceptions.RocketSwaggerException;
import fr.irun.openapi.swagger.mock.GenericMock;
import fr.irun.openapi.swagger.mock.ParameterizedTypeMock;
import fr.irun.openapi.swagger.mock.PojoMock;
import fr.irun.openapi.swagger.mock.TypeMock;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketModelConverterTest {

    private static final String HEXAMON_ENTITY_CLASS_NAME = "fr.irun.hexamon.api.entity.Entity";
    private static final String CMS_NESTED_CLASS_NAME = "fr.irun.cms.api.model.Nested";

    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray(new ModelConverter[0]);
    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private ModelConsolidation fluxModelConsolidation;
    private ModelConsolidation entityModelConsolidation;
    private ModelConsolidation nestedModelConsolidation;
    private ModelConsolidation monoModelConsolidation;
    private ModelConsolidation standardModelConsolidation;

    private ModelConverterContext context;
    private TypeFactory typeFactory;

    private RocketModelConverter tested;

    @BeforeEach
    void setUp() {
        fluxModelConsolidation = mock(ModelConsolidation.class);
        entityModelConsolidation = mock(ModelConsolidation.class);
        nestedModelConsolidation = mock(ModelConsolidation.class);
        monoModelConsolidation = mock(ModelConsolidation.class);
        standardModelConsolidation = mock(ModelConsolidation.class);

        context = mock(ModelConverterContext.class);
        // Cannot be mocked since final
        typeFactory = TypeFactory.defaultInstance();

        // types
        when(fluxModelConsolidation.getModelType()).thenReturn(ModelEnum.FLUX);
        when(entityModelConsolidation.getModelType()).thenReturn(ModelEnum.ENTITY);
        when(nestedModelConsolidation.getModelType()).thenReturn(ModelEnum.NESTED);
        when(monoModelConsolidation.getModelType()).thenReturn(ModelEnum.MONO);
        when(standardModelConsolidation.getModelType()).thenReturn(ModelEnum.STANDARD);

        tested = new RocketModelConverter(
                Arrays.asList(fluxModelConsolidation, entityModelConsolidation,
                        nestedModelConsolidation, monoModelConsolidation,
                        standardModelConsolidation),
                typeFactory);
    }

    @Test
    void resolvePropertyPojo() {
        final RefProperty expectedOutProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any()))
                .thenReturn(expectedOutProperty);

        final Type type = typeFactory.constructType(PojoMock.class);
        Property property = tested.resolveProperty(type, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(type), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(same(null));
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateProperty(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateProperty(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateProperty(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateProperty(any());
        assertThat(property).isSameAs(expectedOutProperty);
    }

    @Test
    void resolvePojo() {
        final RefModel expectedOutModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(expectedOutModel);

        final Type type = typeFactory.constructType(PojoMock.class);
        Model outModel = tested.resolve(type, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(type), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(same(null));
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateModel(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateModel(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateModel(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateModel(any());
        assertThat(outModel).isSameAs(expectedOutModel);
    }


    @Test
    void resolvePropertyGenericPojo() {
        final RefProperty expectedOutProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any()))
                .thenReturn(expectedOutProperty);
        final Type innerType = typeFactory.constructType(PojoMock.class);
        final Type genericType = new ParameterizedTypeMock(GenericMock.class.getName(), innerType);
        Property property = tested.resolveProperty(genericType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(genericType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(same(null));
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateProperty(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateProperty(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateProperty(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateProperty(any());
        assertThat(property).isSameAs(expectedOutProperty);
    }

    @Test
    void resolveGenericPojo() {
        final RefModel expectedOutModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(expectedOutModel);

        final Type innerType = typeFactory.constructType(PojoMock.class);
        final Type genericType = new ParameterizedTypeMock(GenericMock.class.getName(), innerType);
        Model outModel = tested.resolve(genericType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(genericType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(same(null));
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateModel(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateModel(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateModel(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateModel(any());
        assertThat(outModel).isSameAs(expectedOutModel);
    }


    @Test
    void resolvePropertyMonoPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty monoProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any()))
                .thenReturn(baseProperty);
        when(monoModelConsolidation.consolidateProperty(any())).thenReturn(monoProperty);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type monoType = typeFactory.constructSimpleType(Mono.class, new JavaType[]{innerType});
        Property property = tested.resolveProperty(monoType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(monoModelConsolidation).setContext(same(monoType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(monoModelConsolidation).consolidateProperty(same(baseProperty));
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateProperty(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateProperty(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateProperty(any());
        assertThat(property).isSameAs(monoProperty);
    }

    @Test
    void resolveMonoPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel monoModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(monoModelConsolidation.consolidateModel(any())).thenReturn(monoModel);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type monoType = typeFactory.constructSimpleType(Mono.class, new JavaType[]{innerType});
        Model outModel = tested.resolve(monoType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(monoModelConsolidation).setContext(same(monoType), same(context), same(null), same(ITERATOR));
        verify(monoModelConsolidation).consolidateModel(same(baseModel));
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateModel(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateModel(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateModel(any());
        assertThat(outModel).isSameAs(monoModel);
    }

    @Test
    void resolvePropertyFluxPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty fluxProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any())).thenReturn(baseProperty);
        when(fluxModelConsolidation.consolidateProperty(any())).thenReturn(fluxProperty);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type fluxType = typeFactory.constructSimpleType(Flux.class, new JavaType[]{innerType});
        Property property = tested.resolveProperty(fluxType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateProperty(any());
        verify(fluxModelConsolidation).setContext(same(fluxType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(fluxModelConsolidation).consolidateProperty(same(baseProperty));
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateProperty(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateProperty(any());
        assertThat(property).isSameAs(fluxProperty);
    }

    @Test
    void resolveFluxPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel fluxModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(fluxModelConsolidation.consolidateModel(any())).thenReturn(fluxModel);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type fluxType = typeFactory.constructSimpleType(Flux.class, new JavaType[]{innerType});
        Model outModel = tested.resolve(fluxType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateModel(any());
        verify(fluxModelConsolidation).setContext(same(fluxType), same(context), same(null), same(ITERATOR));
        verify(fluxModelConsolidation).consolidateModel(same(baseModel));
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateModel(any());
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateModel(any());
        assertThat(outModel).isSameAs(fluxModel);
    }

    @Test
    void resolvePropertyEntityPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty entityProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any())).thenReturn(baseProperty);
        when(entityModelConsolidation.consolidateProperty(any())).thenReturn(entityProperty);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, innerType);
        Property property = tested.resolveProperty(entityType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateProperty(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateProperty(any());
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(entityModelConsolidation).consolidateProperty(same(baseProperty));
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateProperty(any());
        assertThat(property).isSameAs(entityProperty);
    }

    @Test
    void resolveEntityPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel entityModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(entityModelConsolidation.consolidateModel(any())).thenReturn(entityModel);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, innerType);
        Model outModel = tested.resolve(entityType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateModel(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateModel(any());
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(null), same(ITERATOR));
        verify(entityModelConsolidation).consolidateModel(same(baseModel));
        verify(nestedModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(nestedModelConsolidation, never()).consolidateModel(any());
        assertThat(outModel).isSameAs(entityModel);
    }


    @Test
    void resolvePropertyNestedPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty nestedProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any())).thenReturn(baseProperty);
        when(nestedModelConsolidation.consolidateProperty(any())).thenReturn(nestedProperty);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type nestedType = new ParameterizedTypeMock(CMS_NESTED_CLASS_NAME, innerType);
        Property property = tested.resolveProperty(nestedType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateProperty(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateProperty(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateProperty(any());
        verify(nestedModelConsolidation).setContext(same(nestedType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(nestedModelConsolidation).consolidateProperty(same(baseProperty));
        assertThat(property).isSameAs(nestedProperty);
    }

    @Test
    void resolveNestedPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel nestedModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(nestedModelConsolidation.consolidateModel(any())).thenReturn(nestedModel);

        final JavaType innerType = typeFactory.constructType(PojoMock.class);
        final Type nestedType = new ParameterizedTypeMock(CMS_NESTED_CLASS_NAME, innerType);
        Model outModel = tested.resolve(nestedType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateModel(any());
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateModel(any());
        verify(entityModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(entityModelConsolidation, never()).consolidateModel(any());
        verify(nestedModelConsolidation).setContext(same(nestedType), same(context), same(null), same(ITERATOR));
        verify(nestedModelConsolidation).consolidateModel(same(baseModel));
        assertThat(outModel).isSameAs(nestedModel);
    }

    /*
     * More complex test - input type: Mono<Nested<Entity<POJO>>>
     */
    @Test
    void resolvePropertyMonoNestedEntityPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty entityProperty = new RefProperty();
        final RefProperty nestedProperty = new RefProperty();
        final RefProperty monoProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any())).thenReturn(baseProperty);
        when(entityModelConsolidation.consolidateProperty(any())).thenReturn(entityProperty);
        when(nestedModelConsolidation.consolidateProperty(any())).thenReturn(nestedProperty);
        when(monoModelConsolidation.consolidateProperty(any())).thenReturn(monoProperty);

        final JavaType baseType = typeFactory.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, baseType);
        final Type nestedType = new ParameterizedTypeMock(CMS_NESTED_CLASS_NAME, entityType);
        final Type monoType = new ParameterizedTypeMock(Mono.class.getName(), nestedType);
        Property property = tested.resolveProperty(monoType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(baseType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(entityModelConsolidation).consolidateProperty(same(baseProperty));
        verify(nestedModelConsolidation).setContext(same(nestedType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(nestedModelConsolidation).consolidateProperty(same(entityProperty));
        verify(monoModelConsolidation).setContext(same(monoType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(monoModelConsolidation).consolidateProperty(nestedProperty);
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateProperty(any());
        assertThat(property).isSameAs(monoProperty);
    }

    /*
     * More complex test - input type: Mono<Nested<Entity<POJO>>>
     */
    @Test
    void resolveMonoNestedEntityPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel entityModel = new RefModel();
        final RefModel nestedModel = new RefModel();
        final RefModel monoModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(entityModelConsolidation.consolidateModel(any())).thenReturn(entityModel);
        when(nestedModelConsolidation.consolidateModel(any())).thenReturn(nestedModel);
        when(monoModelConsolidation.consolidateModel(any())).thenReturn(monoModel);

        final JavaType baseType = typeFactory.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, baseType);
        final Type nestedType = new ParameterizedTypeMock(CMS_NESTED_CLASS_NAME, entityType);
        final Type monoType = new ParameterizedTypeMock(Mono.class.getName(), nestedType);
        Model model = tested.resolve(monoType, context, ITERATOR);

        assertThat(model).isNotNull();
        verify(standardModelConsolidation).setContext(same(baseType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(null), same(ITERATOR));
        verify(entityModelConsolidation).consolidateModel(same(baseModel));
        verify(nestedModelConsolidation).setContext(same(nestedType), same(context), same(null), same(ITERATOR));
        verify(nestedModelConsolidation).consolidateModel(same(entityModel));
        verify(monoModelConsolidation).setContext(same(monoType), same(context), same(null), same(ITERATOR));
        verify(monoModelConsolidation).consolidateModel(same(nestedModel));
        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(fluxModelConsolidation, never()).consolidateModel(any());
        assertThat(model).isSameAs(monoModel);
    }


    /*
     * More complex test - input type: Flux<Nested<Entity<POJO>>>
     */
    @Test
    void resolvePropertyFluxNestedEntityPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty entityProperty = new RefProperty();
        final RefProperty nestedProperty = new RefProperty();
        final RefProperty fluxProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any())).thenReturn(baseProperty);
        when(entityModelConsolidation.consolidateProperty(any())).thenReturn(entityProperty);
        when(nestedModelConsolidation.consolidateProperty(any())).thenReturn(nestedProperty);
        when(fluxModelConsolidation.consolidateProperty(any())).thenReturn(fluxProperty);

        final JavaType baseType = typeFactory.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, baseType);
        final Type nestedType = new ParameterizedTypeMock(CMS_NESTED_CLASS_NAME, entityType);
        final Type fluxType = new ParameterizedTypeMock(Flux.class.getName(), nestedType);
        Property property = tested.resolveProperty(fluxType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(baseType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(entityModelConsolidation).consolidateProperty(same(baseProperty));
        verify(nestedModelConsolidation).setContext(same(nestedType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(nestedModelConsolidation).consolidateProperty(same(entityProperty));
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateProperty(any());
        verify(fluxModelConsolidation).setContext(same(fluxType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(fluxModelConsolidation).consolidateProperty(same(nestedProperty));
        assertThat(property).isSameAs(fluxProperty);
    }

    /*
     * More complex test - input type: Flux<Nested<Entity<POJO>>>
     */
    @Test
    void resolveFluxNestedEntityPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel entityModel = new RefModel();
        final RefModel nestedModel = new RefModel();
        final RefModel fluxModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(entityModelConsolidation.consolidateModel(any())).thenReturn(entityModel);
        when(nestedModelConsolidation.consolidateModel(any())).thenReturn(nestedModel);
        when(fluxModelConsolidation.consolidateModel(any())).thenReturn(fluxModel);

        final JavaType baseType = typeFactory.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, baseType);
        final Type nestedType = new ParameterizedTypeMock(CMS_NESTED_CLASS_NAME, entityType);
        final Type fluxType = new ParameterizedTypeMock(Flux.class.getName(), nestedType);
        Model model = tested.resolve(fluxType, context, ITERATOR);

        assertThat(model).isNotNull();
        verify(standardModelConsolidation).setContext(same(baseType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(null), same(ITERATOR));
        verify(entityModelConsolidation).consolidateModel(same(baseModel));
        verify(nestedModelConsolidation).setContext(same(nestedType), same(context), same(null), same(ITERATOR));
        verify(nestedModelConsolidation).consolidateModel(same(entityModel));
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateModel(any());
        verify(fluxModelConsolidation).setContext(same(fluxType), same(context), same(null), same(ITERATOR));
        verify(fluxModelConsolidation).consolidateModel(same(nestedModel));
        assertThat(model).isSameAs(fluxModel);
    }

    // Test error cases.

    @Test
    void resolvePropertyNoModelConsolidation() {
        tested = new RocketModelConverter(new ArrayList<>(0), TypeFactory.defaultInstance());

        assertThrows(RocketSwaggerException.class, () -> tested.resolveProperty(String.class, context, ANNOTATIONS, ITERATOR));
    }

    @Test
    void resolveNoModelConsolidation() {
        tested = new RocketModelConverter(new ArrayList<>(0), TypeFactory.defaultInstance());

        assertThrows(RocketSwaggerException.class, () -> tested.resolve(String.class, context, ITERATOR));
    }

    @Test
    void resolvePropertyNulltype() {
        assertThrows(RocketSwaggerException.class, () -> tested.resolveProperty(null, context, ANNOTATIONS, ITERATOR));
    }

    @Test
    void resolveNoModelNulltype() {
        assertThrows(RocketSwaggerException.class, () -> tested.resolve(null, context, ITERATOR));
    }

    @Test
    void resolvePropertyNonStandardFirstType() {
        final Type type = new TypeMock(HEXAMON_ENTITY_CLASS_NAME);

        assertThrows(RocketSwaggerException.class, () -> tested.resolveProperty(type, context, ANNOTATIONS, ITERATOR));
    }

    @Test
    void resolveNonStandardFirstType() {
        final Type type = new TypeMock(HEXAMON_ENTITY_CLASS_NAME);

        assertThrows(RocketSwaggerException.class, () -> tested.resolve(type, context, ITERATOR));
    }

}
