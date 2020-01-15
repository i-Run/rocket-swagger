package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Default Model resolver.
 */
public class DefaultModelResolver implements RocketModelResolver {

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.DEFAULT;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            return iterator.next().resolveProperty(type, context, annotations, iterator);
        }
        return null;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            return iterator.next().resolve(type, modelConverterContext, iterator);
        }
        return null;
    }
}
