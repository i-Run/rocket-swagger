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

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

/**
 * Implementation of the consolidation for an entity Model.
 */
public class EntityModelConsolidation implements ModelConsolidation {

    private static final String ENTITY_FIELD_NAME = "entity";

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
        return baseConverter.resolveProperty(entityType, context, annotations, converterIterator);
    }

    @Override
    public Model consolidateModel(Model model) {
        Type innerElementType = ModelConversionUtils.extractGenericFirstInnerType(entityType);
        final String modelName = ModelConversionUtils.getSimpleClassName(innerElementType);

        return Optional.ofNullable(model)
                .map(m -> ModelConversionUtils.copyModelWithoutProperties(modelName, m))
                .<Model>map(m -> {
                    final Class<?> entityClass = typeFactory.constructType(entityType).getRawClass();
                    final Class<?> innerClass = typeFactory.constructType(innerElementType).getRawClass();
                    putEntityClassPropertiesInModel(entityClass, m);
                    putPojoClassPropertiesInModel(innerClass, m);
                    return m;
                })
                .orElse(model);
    }


    private void putEntityClassPropertiesInModel(Class<?> inputClass, ModelImpl outputModel) {

        Arrays.stream(inputClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !ENTITY_FIELD_NAME.equals(field.getName()))
                .forEach(field -> {
                    String propertyKey = "_" + field.getName();
                    Property propertyValue = baseConverter.resolveProperty(field.getType(), context, annotations, converterIterator);
                    outputModel.addProperty(propertyKey, propertyValue);
                });
    }

    private void putPojoClassPropertiesInModel(Class<?> inputClass, ModelImpl outputModel) {

        Arrays.stream(inputClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .forEach(field -> {
                    String propertyKey = field.getName();
                    Property propertyValue = baseConverter.resolveProperty(field.getType(), context, annotations, converterIterator);
                    outputModel.addProperty(propertyKey, propertyValue);
                });
    }
}
