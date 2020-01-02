package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized Model resolver for a type wrapping another (e.g. Mono or ResponseEntity).
 */
public class GenericModelResolver implements RocketModelResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericModelResolver.class);

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.WRAP_GENERIC;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            // Property of type MyWrapper<T> converted to T
            final Type innerMonoType = ModelConversionUtils.extractGenericFirstInnerType(type);
            final ModelConverter converter = iterator.next();
            LOGGER.trace("Strategy {}: resolve property type {} with {}", getResolutionStrategy(), innerMonoType, converter.getClass());
            return converter.resolveProperty(innerMonoType, context, annotations, iterator);
        }
        return null;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            final Type innerMonoType = ModelConversionUtils.extractGenericFirstInnerType(type);
            final ModelConverter converter = iterator.next();
            LOGGER.trace("Strategy {}: resolve model type {} with {}", getResolutionStrategy(), innerMonoType, converter.getClass());
            return converter.resolve(innerMonoType, modelConverterContext, iterator);
        }
        return null;
    }
}
