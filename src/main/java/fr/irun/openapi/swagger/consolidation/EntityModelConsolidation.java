package fr.irun.openapi.swagger.consolidation;

import com.fasterxml.jackson.databind.type.TypeFactory;
import fr.irun.openapi.swagger.converter.BaseModelConverter;
import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Implementation of the consolidation for an entity Model.
 */
public class EntityModelConsolidation implements ModelConsolidation {

    private static final String REFERENCE_SEPARATOR = "/";
    private static final String ENTITY_FIELD_NAME = "entity";
    private static final String ENTITY_SUFFIX = "Entity";

    private final TypeFactory typeFactory;
    private final ModelConverter baseConverter;

    private Type entityType;
    private ModelConverterContext context;
    private Annotation[] annotations;
    private Iterator<ModelConverter> converterIterator;

    public EntityModelConsolidation() {
        this(TypeFactory.defaultInstance(), new BaseModelConverter());
    }

    EntityModelConsolidation(TypeFactory typeFactory, ModelConverter baseConverter) {
        this.typeFactory = typeFactory;
        this.baseConverter = baseConverter;
    }


    @Override
    public ModelEnum getModelType() {
        return ModelEnum.ENTITY;
    }

    @Override
    public void setContext(Type baseType, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> converterChain) {
        entityType = baseType;
        this.context = context;
        converterIterator = converterChain;
        this.annotations = annotations;
    }

    @Override
    public Property consolidateProperty(Property property) {
        if (property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            refProperty.set$ref(refProperty.get$ref() + ENTITY_SUFFIX);
        }
        return property;
    }

    @Override
    public Model consolidateModel(Model model) {
        Type innerElementType = ModelConversionUtils.extractGenericFirstInnerType(entityType);

        if (model != null && innerElementType != null) {
            final String baseModelReference = model.getReference();
            final String baseModelName = ModelConversionUtils.extractLastSplitResult(baseModelReference, REFERENCE_SEPARATOR);
            ModelImpl outputModel = ModelConversionUtils.copyModel(
                    baseModelName + ENTITY_SUFFIX,
                    baseModelReference + ENTITY_SUFFIX,
                    model);

            Class<?> entityClass = typeFactory.constructType(entityType).getRawClass();
            Class<?> innerClass = typeFactory.constructType(innerElementType).getRawClass();
            putEntityClassPropertiesInModel(entityClass, outputModel, context, converterIterator);
            putPojoClassPropertiesInModel(innerClass, outputModel, context, converterIterator);
            return outputModel;
        }
        return model;
    }


    private void putEntityClassPropertiesInModel(Class<?> inputClass,
                                                 ModelImpl outputModel,
                                                 ModelConverterContext modelConverterContext,
                                                 Iterator<ModelConverter> iterator) {

        Stream<Field> fieldsToUse = Arrays.stream(inputClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers())
                        && !ENTITY_FIELD_NAME.equals(field.getName()));
        fieldsToUse.forEach(field -> {
            String propertyKey = "_" + field.getName();
            Property propertyValue = baseConverter.resolveProperty(field.getType(), modelConverterContext, annotations, iterator);
            outputModel.addProperty(propertyKey, propertyValue);
        });
    }

    private void putPojoClassPropertiesInModel(Class<?> inputClass,
                                               ModelImpl outputModel,
                                               ModelConverterContext modelConverterContext,
                                               Iterator<ModelConverter> iterator) {
        Stream<Field> fieldsToUse = Arrays.stream(inputClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()));
        fieldsToUse.forEach(field -> {
            String propertyKey = field.getName();
            Property propertyValue = baseConverter.resolveProperty(field.getType(), modelConverterContext, annotations, iterator);
            outputModel.addProperty(propertyKey, propertyValue);
        });

    }
}
