package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized Model converter used to :
 * <ul>
 * <li>Correctly parse Instants and DateTimes tp JSON</li>
 * <li>Manage the Mono and the Flux of Spring reactor.</li>
 * </ul>
 */
public class ReactorModelConverter implements ModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorModelConverter.class);

    /**
     * Default ModelConverter used if this one does not manage a type.
     */
    private final ModelConverter baseConverter = new DateTimeModelConverter();

    private final ModelConverter entityModelConverter = new HexamonEntityModelConverter();

    private final TypeFactory typeFactory = TypeFactory.defaultInstance();


    @Override
    public Property resolveProperty(Type type, ModelConverterContext context,
                                    Annotation[] annotations, Iterator<ModelConverter> chain) {
        Type analyzedType = type;
        if (ModelConversionUtils.doesTypeMatchAnyClass(type, Flux.class)) {
            trace("Resolve property - Detected Spring reactor type: " + type);
            Type innerType = ModelConversionUtils.extractGenericFirstInnerType(type);
            JavaType javaType = typeFactory.constructType(innerType);
            analyzedType = typeFactory.constructArrayType(javaType);

        } else if (ModelConversionUtils.doesTypeMatchAnyClass(type, Mono.class)) {
            Type innerType = ModelConversionUtils.extractGenericFirstInnerType(type);
            analyzedType = typeFactory.constructType(innerType);
        }

        Property property;
        if (ModelConversionUtils.isHexamonEntityType(analyzedType)) {
            property = entityModelConverter.resolveProperty(analyzedType, context, annotations, chain);
        } else {
            property = baseConverter.resolveProperty(analyzedType, context, annotations, chain);
        }
        return property;
    }


    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        Type realUsedType = type;
        // Do not consider the Mono and Flux classes.
        if (ModelConversionUtils.doesTypeMatchAnyClass(realUsedType, Flux.class, Mono.class)) {
            trace("Resolve Model - Detected Spring Reactor type: " + realUsedType);
            realUsedType = ModelConversionUtils.extractGenericFirstInnerType(type);
        }
        Model outModel;
        if (ModelConversionUtils.isHexamonEntityType(realUsedType)) {
            outModel = entityModelConverter.resolve(realUsedType, modelConverterContext, iterator);
        } else {
            outModel = baseConverter.resolve(type, modelConverterContext, iterator);
        }
        return outModel;
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
