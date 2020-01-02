package fr.irun.openapi.swagger.resolver;

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
 * Default Model resolver.
 */
public class DefaultModelResolver implements RocketModelResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelResolver.class);

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.DEFAULT;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            final ModelConverter converter = iterator.next();
            LOGGER.trace("Strategy {}: resolve property type {} with {}", getResolutionStrategy(), type, converter.getClass());
            return converter.resolveProperty(type, context, annotations, iterator);
        }
        return null;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            final ModelConverter converter = iterator.next();
            LOGGER.trace("Strategy {}: resolve model type {} with {}", getResolutionStrategy(), type, converter.getClass());
            return converter.resolve(type, modelConverterContext, iterator);
        }
        return null;
    }
}
