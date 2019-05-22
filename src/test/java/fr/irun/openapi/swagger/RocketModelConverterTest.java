package fr.irun.openapi.swagger;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.consolidation.ModelConsolidation;
import fr.irun.openapi.swagger.exceptions.RocketSwaggerException;
import fr.irun.openapi.swagger.mock.GenericMock;
import fr.irun.openapi.swagger.mock.ParameterizedTypeMock;
import fr.irun.openapi.swagger.mock.PojoMock;
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class RocketModelConverterTest {

    private static final String HEXAMON_ENTITY_CLASS_NAME = "fr.irun.hexamon.api.entity.Entity";
    private static final String HEXAMON_PAGE_CLASS_NAME = "fr.irun.cms.api.model.Page";

    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray(new ModelConverter[0]);
    private static final Annotation[] ANNOTATIONS = new Annotation[0];

    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    private ModelConsolidation fluxModelConsolidation;
    private ModelConsolidation entityModelConsolidation;
    private ModelConsolidation monoModelConsolidation;
    private ModelConsolidation standardModelConsolidation;

    private ModelConverterContext context;


    private RocketModelConverter tested;

    @BeforeEach
    void setUp() {
        fluxModelConsolidation = mock(ModelConsolidation.class);
        entityModelConsolidation = mock(ModelConsolidation.class);
        monoModelConsolidation = mock(ModelConsolidation.class);
        standardModelConsolidation = mock(ModelConsolidation.class);
        context = mock(ModelConverterContext.class);

        // types
        when(fluxModelConsolidation.getModelType()).thenReturn(ModelEnum.FLUX);
        when(entityModelConsolidation.getModelType()).thenReturn(ModelEnum.ENTITY);
        when(monoModelConsolidation.getModelType()).thenReturn(ModelEnum.MONO);
        when(standardModelConsolidation.getModelType()).thenReturn(ModelEnum.STANDARD);

        tested = new RocketModelConverter(
                Arrays.asList(fluxModelConsolidation, entityModelConsolidation,
                        monoModelConsolidation, standardModelConsolidation));

        // Verify the constructor
        verify(fluxModelConsolidation).getModelType();
        verify(entityModelConsolidation).getModelType();
        verify(monoModelConsolidation).getModelType();
        verify(standardModelConsolidation).getModelType();
    }

    /*
     * POJO
     */
    @Test
    void resolvePropertyPojo() {
        final RefProperty expectedOutProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any()))
                .thenReturn(expectedOutProperty);

        final Type type = TYPE_FACTORY.constructType(PojoMock.class);
        Property property = tested.resolveProperty(type, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(type), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(same(null));
        verifyZeroInteractions(monoModelConsolidation, fluxModelConsolidation, entityModelConsolidation);

        assertThat(property).isSameAs(expectedOutProperty);
    }

    /*
     * POJO
     */
    @Test
    void resolvePojo() {
        final RefModel expectedOutModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(expectedOutModel);

        final Type type = TYPE_FACTORY.constructType(PojoMock.class);
        Model outModel = tested.resolve(type, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(type), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(same(null));

        verifyZeroInteractions(monoModelConsolidation, fluxModelConsolidation, entityModelConsolidation);
        assertThat(outModel).isSameAs(expectedOutModel);
    }

    /*
     * POJO<T>
     */
    @Test
    void resolvePropertyGenericPojo() {
        final RefProperty expectedOutProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any()))
                .thenReturn(expectedOutProperty);
        final Type innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type genericType = new ParameterizedTypeMock(GenericMock.class.getName(), innerType);
        Property property = tested.resolveProperty(genericType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(genericType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(same(null));
        verifyZeroInteractions(monoModelConsolidation, fluxModelConsolidation, entityModelConsolidation);
        assertThat(property).isSameAs(expectedOutProperty);
    }

    /*
     * POJO<T>
     */
    @Test
    void resolveGenericPojo() {
        final RefModel expectedOutModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(expectedOutModel);

        final Type innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type genericType = new ParameterizedTypeMock(GenericMock.class.getName(), innerType);
        Model outModel = tested.resolve(genericType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(genericType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(same(null));
        verifyZeroInteractions(monoModelConsolidation, fluxModelConsolidation, entityModelConsolidation);
        assertThat(outModel).isSameAs(expectedOutModel);
    }


    /*
     * Mono<POJO>
     */
    @Test
    void resolvePropertyMonoPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty monoProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any()))
                .thenReturn(baseProperty);
        when(monoModelConsolidation.consolidateProperty(any())).thenReturn(monoProperty);

        final JavaType innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type monoType = TYPE_FACTORY.constructSimpleType(Mono.class, new JavaType[]{innerType});
        Property property = tested.resolveProperty(monoType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(monoModelConsolidation).setContext(same(monoType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(monoModelConsolidation).consolidateProperty(same(baseProperty));
        verifyZeroInteractions(fluxModelConsolidation, entityModelConsolidation);
        assertThat(property).isSameAs(monoProperty);
    }

    /*
     * Mono<POJO>
     */
    @Test
    void resolveMonoPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel monoModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(monoModelConsolidation.consolidateModel(any())).thenReturn(monoModel);

        final JavaType innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type monoType = TYPE_FACTORY.constructSimpleType(Mono.class, new JavaType[]{innerType});
        Model outModel = tested.resolve(monoType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(monoModelConsolidation).setContext(same(monoType), same(context), same(null), same(ITERATOR));
        verify(monoModelConsolidation).consolidateModel(same(baseModel));
        verifyZeroInteractions(fluxModelConsolidation, entityModelConsolidation);
        assertThat(outModel).isSameAs(monoModel);
    }

    /*
     * Flux<POJO>
     */
    @Test
    void resolvePropertyFluxPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty fluxProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any())).thenReturn(baseProperty);
        when(fluxModelConsolidation.consolidateProperty(any())).thenReturn(fluxProperty);

        final JavaType innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type fluxType = TYPE_FACTORY.constructSimpleType(Flux.class, new JavaType[]{innerType});
        Property property = tested.resolveProperty(fluxType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateProperty(any());
        verify(fluxModelConsolidation).setContext(same(fluxType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(fluxModelConsolidation).consolidateProperty(same(baseProperty));
        verifyZeroInteractions(entityModelConsolidation);
        assertThat(property).isSameAs(fluxProperty);
    }

    /*
     * Mono<POJO>
     */
    @Test
    void resolveFluxPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel fluxModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(fluxModelConsolidation.consolidateModel(any())).thenReturn(fluxModel);

        final JavaType innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type fluxType = TYPE_FACTORY.constructSimpleType(Flux.class, new JavaType[]{innerType});
        Model outModel = tested.resolve(fluxType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
        verify(monoModelConsolidation, never()).consolidateModel(any());
        verify(fluxModelConsolidation).setContext(same(fluxType), same(context), same(null), same(ITERATOR));
        verify(fluxModelConsolidation).consolidateModel(same(baseModel));
        verifyZeroInteractions(entityModelConsolidation);
        assertThat(outModel).isSameAs(fluxModel);
    }

    /*
     * Entity<POJO>
     */
    @Test
    void resolvePropertyEntityPojo() {
        final RefProperty baseProperty = new RefProperty();
        final RefProperty entityProperty = new RefProperty();
        when(standardModelConsolidation.consolidateProperty(any())).thenReturn(baseProperty);
        when(entityModelConsolidation.consolidateProperty(any())).thenReturn(entityProperty);

        final JavaType innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, innerType);
        Property property = tested.resolveProperty(entityType, context, ANNOTATIONS, ITERATOR);

        assertThat(property).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(standardModelConsolidation).consolidateProperty(null);
//        verify(monoModelConsolidation, never()).setContext(any(), any(), any(), any());
//        verify(monoModelConsolidation, never()).consolidateProperty(any());
//        verify(fluxModelConsolidation, never()).setContext(any(), any(), any(), any());
//        verify(fluxModelConsolidation, never()).consolidateProperty(any());
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(ANNOTATIONS), same(ITERATOR));
        verify(entityModelConsolidation).consolidateProperty(same(baseProperty));
        verifyZeroInteractions(monoModelConsolidation, fluxModelConsolidation);
        assertThat(property).isSameAs(entityProperty);
    }

    /*
     * Mono<POJO>
     */
    @Test
    void resolveEntityPojo() {
        final RefModel baseModel = new RefModel();
        final RefModel entityModel = new RefModel();
        when(standardModelConsolidation.consolidateModel(any())).thenReturn(baseModel);
        when(entityModelConsolidation.consolidateModel(any())).thenReturn(entityModel);

        final JavaType innerType = TYPE_FACTORY.constructType(PojoMock.class);
        final Type entityType = new ParameterizedTypeMock(HEXAMON_ENTITY_CLASS_NAME, innerType);
        Model outModel = tested.resolve(entityType, context, ITERATOR);

        assertThat(outModel).isNotNull();
        verify(standardModelConsolidation).setContext(same(innerType), same(context), same(null), same(ITERATOR));
        verify(standardModelConsolidation).consolidateModel(null);
        verifyZeroInteractions(monoModelConsolidation, fluxModelConsolidation);
        verify(entityModelConsolidation).setContext(same(entityType), same(context), same(null), same(ITERATOR));
        verify(entityModelConsolidation).consolidateModel(same(baseModel));

        assertThat(outModel).isSameAs(entityModel);
    }


    // Test error cases.

    @Test
    void resolvePropertyNoModelConsolidation() {
        tested = new RocketModelConverter(new ArrayList<>(0));

        assertThrows(RocketSwaggerException.class, () -> tested.resolveProperty(String.class, context, ANNOTATIONS, ITERATOR));
    }

    @Test
    void resolveNoModelConsolidation() {
        tested = new RocketModelConverter(new ArrayList<>(0));

        assertThrows(RocketSwaggerException.class, () -> tested.resolve(String.class, context, ITERATOR));
    }

    @Test
    void resolvePropertyNulltype() {
        final Property actual = tested.resolveProperty(null, context, ANNOTATIONS, ITERATOR);
        assertThat(actual).isNull();
    }

    @Test
    void resolveNoModelNulltype() {
        final Model actual = tested.resolve(null, context, ITERATOR);
        assertThat(actual).isNull();
    }

}
