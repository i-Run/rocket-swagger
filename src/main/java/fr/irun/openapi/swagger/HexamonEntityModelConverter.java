package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Specific model converter used to convert hexamon entities.
 * Example: for the model "Page" with id "#/definitions/Page",
 * this class will generate the model "PageEntity" with "#/definitions/PageEntity", including the history fields.
 */
class HexamonEntityModelConverter implements ModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HexamonEntityModelConverter.class);

    /**
     * Name of the field ignored when parsing the hexamon entity class.
     */
    private static final String ENTITY_FIELD_NAME = "entity";

    /**
     * Suffix of the model generated
     */
    private static final String ENTITY_REFERENCE_SUFFIX = "Entity";

    private final ModelConverter baseConverter;

    private final TypeFactory typeFactory;

    HexamonEntityModelConverter() {
        this(new DateTimeModelConverter(), TypeFactory.defaultInstance());
    }

    HexamonEntityModelConverter(ModelConverter baseConverter, TypeFactory typeFactory) {
        this.baseConverter = baseConverter;
        this.typeFactory = typeFactory;
    }


    @Override
    public Property resolveProperty(Type type,
                                    ModelConverterContext modelConverterContext,
                                    Annotation[] annotations,
                                    Iterator<ModelConverter> iterator) {
        Property property;
        Type innerType = ModelConversionUtils.extractGenericFirstInnerType(type);
        trace("Detected hexamon entity: " + type);
        trace("Inner type of hexamon entity: " + innerType);
        if (innerType == null) {
            property = baseConverter.resolveProperty(type, modelConverterContext, annotations, iterator);
        } else {
            property = entityTypeToProperty(innerType, modelConverterContext, annotations, iterator);
        }
        return property;
    }

    /**
     * Convert the inner type of the entity into a property.
     *
     * @param innerType             Inner type of the Entity.
     * @param modelConverterContext context for conversion.
     * @param annotations           annotations.
     * @param iterator              iterator under converters.
     * @return The built property (with reference including the suffix "Entity").
     */
    private Property entityTypeToProperty(Type innerType,
                                          ModelConverterContext modelConverterContext,
                                          Annotation[] annotations,
                                          Iterator<ModelConverter> iterator) {
        Property baseProperty = baseConverter.resolveProperty(innerType, modelConverterContext, annotations, iterator);
        if (baseProperty instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) baseProperty;
            refProperty.set$ref(refProperty.get$ref() + ENTITY_REFERENCE_SUFFIX);
        }
        return baseProperty;
    }


    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        Model model;

        trace("Resolve Model - Detected Hexamon Entity type: " + type);
        Type innerType = ModelConversionUtils.extractGenericFirstInnerType(type);
        trace("Resolve Model - Inner type of Hexamon entity: " + innerType);

        if (innerType == null) {
            model = baseConverter.resolve(type, modelConverterContext, iterator);
        } else {
            model = entityTypeToModel(type, innerType, modelConverterContext, iterator);
        }
        return model;
    }

    /**
     * Convert the type of an hexamon entity to a custom model.
     *
     * @param hexamonEntityType     Type of the hexamon entity.
     * @param innerType             Inner type managed by the hexamon entity.
     * @param modelConverterContext context for conversion.
     * @param iterator              Iterator under the model converters.
     * @return The model related to the given type (with "Entity" suffix into its reference and history fields).
     */
    private Model entityTypeToModel(Type hexamonEntityType,
                                    Type innerType,
                                    ModelConverterContext modelConverterContext,
                                    Iterator<ModelConverter> iterator) {
        Class<?> entityClass = typeFactory.constructType(hexamonEntityType).getRawClass();
        Class<?> innerClass = typeFactory.constructType(innerType).getRawClass();

        Model baseModel = baseConverter.resolve(innerType, modelConverterContext, iterator);
        ModelImpl outputModel = copyModel(baseModel, innerClass.getSimpleName());

        putEntityClassPropertiesInModel(entityClass, outputModel, modelConverterContext, iterator);
        putPojoClassPropertiesInModel(innerClass, outputModel, modelConverterContext, iterator);

        return outputModel;
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
            Property propertyValue = baseConverter.resolveProperty(field.getType(), modelConverterContext, null, iterator);
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
            Property propertyValue = baseConverter.resolveProperty(field.getType(), modelConverterContext, null, iterator);
            outputModel.addProperty(propertyKey, propertyValue);
        });

    }


    private ModelImpl copyModel(Model baseModel, String baseModelName) {
        ModelImpl model = new ModelImpl();
        model.setName(baseModelName + ENTITY_REFERENCE_SUFFIX);
        model.setDescription(baseModel.getDescription());
        model.setReference(baseModel.getReference() + ENTITY_REFERENCE_SUFFIX);
        model.setTitle(baseModel.getTitle());
        model.setExternalDocs(baseModel.getExternalDocs());
        model.setExample(baseModel.getExample());
        return model;
    }

    /**
     * Trace log if trace is enabled
     *
     * @param message message to trace.
     */
    private static void trace(String message) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(message);
        }
    }

}
