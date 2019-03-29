package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

/**
 * Customized Model converter used to :
 * <ul>
 * <li>Correctly parse Instants and DateTimes tp JSON</li>
 * <li>Manage the Mono and the Flux of Spring reactor.</li>
 * </ul>
 */
public class ReactorModelConverter implements ModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorModelConverter.class);

    private static final String HEXAMON_ENTITY_FIELD_NAME = "entity";

    /**
     * Default ModelConverter used if this one does not manage a type.
     */
    private final ModelConverter baseConverter = new DateTimeModelConverter();


    @Override
    public Property resolveProperty(Type type, ModelConverterContext context,
                                    Annotation[] annotations, Iterator<ModelConverter> chain) {
        Property property = null;

        if (ModelConversionUtils.doesTypeMatchAnyClass(type, Flux.class)) {

            trace("Detected Spring reactor type: " + type.getTypeName());
            // case when the property is a Flux<T> ==> convert to a T[]
            Property innerProperty = getInnerPropertyOfReactorType(type, context, annotations, chain);
            property = new ArrayProperty().items(innerProperty);

        } else if (ModelConversionUtils.doesTypeMatchAnyClass(type, Mono.class)) {

            trace("Detected Spring reactor type: " + type.getTypeName());
            // Case when the property is a Mono<T> ==> convert to a T
            property = getInnerPropertyOfReactorType(type, context, annotations, chain);
        }

        // The property could be null at the end of the conversion.
        if (property == null) {
            // Otherwise call the default model converter
            property = baseConverter.resolveProperty(type, context, annotations, chain);
        }

        return property;
    }

    /**
     * Obtain the Parameter corresponding to an inner type of a reactor class (Mono or Flux).
     *
     * @param reactorType The type of the generic class.
     * @param context     Context of the converter
     * @param annotations Array of the annotations.
     * @param chain       Chain of converter to use.
     * @return The property relative to the type T if the input type corresponds to a class SomeGenericClass&lt;T.&gt;
     */
    private Property getInnerPropertyOfReactorType(Type reactorType, ModelConverterContext context,
                                                   Annotation[] annotations, Iterator<ModelConverter> chain) {
        Property property;
        Type reactorInnerType = ModelConversionUtils.extractGenericFirstInnerType(reactorType);

        if (ModelConversionUtils.isHexamonEntityType(reactorInnerType)) {
            property = computeHexamonEntityProperty(reactorInnerType, context, annotations, chain);
        } else {
            property = baseConverter.resolveProperty(reactorInnerType, context, annotations, chain);
        }

        return property;
    }


    private Property computeHexamonEntityProperty(Type entityType, ModelConverterContext context,
                                                  Annotation[] annotations, Iterator<ModelConverter> chain) {
        Property property = null;
        Type innerEntityType = ModelConversionUtils.extractGenericFirstInnerType(entityType);

        if (innerEntityType != null) {
            property = baseConverter.resolveProperty(innerEntityType, context, annotations, chain);
        }
        return property;
    }


    private String entityFieldName(Map.Entry<String, ?> entry) {
        return "_" + entry.getKey();
    }


    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        Model outModel = null;

        Type realUsedType = type;
        // Do not consider the Mono and Flux classes.
        if (ModelConversionUtils.doesTypeMatchAnyClass(type, Flux.class, Mono.class)) {
            trace("Detected Reactor type: " + type);
            realUsedType = ModelConversionUtils.extractGenericFirstInnerType(type);
        }
        if (realUsedType != null && ModelConversionUtils.isHexamonEntityType(realUsedType)) {
            trace("Detected Entity type: " + realUsedType);
            outModel = resolveHexamonEntityModel(realUsedType, modelConverterContext, iterator);
        }

        if (outModel == null) {
            outModel = baseConverter.resolve(type, modelConverterContext, iterator);
        }
        return outModel;
    }

    private Model resolveHexamonEntityModel(Type hexamonEntityType,
                                            ModelConverterContext modelConverterContext,
                                            Iterator<ModelConverter> iterator) {
        Model outModel = null;
        Type innerEntityType = ModelConversionUtils.extractGenericFirstInnerType(hexamonEntityType);

        JavaType realInnerType = constructEntityType(hexamonEntityType, innerEntityType);

        if (realInnerType != null) {
            outModel = baseConverter.resolve(realInnerType, modelConverterContext, iterator);
        }
        return outModel;
    }


    private JavaType constructEntityType(Type hexamonEntityType, Type innerEntityType) {
        JavaType hexamonEntityJavaType = constructJavaType(hexamonEntityType);
        JavaType innerEntityJavaType = constructJavaType(innerEntityType);

        JavaType outputType = null;
        if (hexamonEntityJavaType != null && innerEntityJavaType != null) {
            outputType = TypeFactory.defaultInstance()
                    .constructSimpleType(hexamonEntityJavaType.getRawClass(), new JavaType[]{innerEntityJavaType});
        }
        return outputType;
    }


    private JavaType constructJavaType(Type inputType) {
        JavaType javaType = null;
        if (inputType != null) {
            javaType = TypeFactory.defaultInstance().constructType(inputType);
        }
        return javaType;
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
