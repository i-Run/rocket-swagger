package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

/**
 * Resolver to resolve a type as a Map.
 */
public class MapModelResolver implements RocketModelResolver {

    private static final Type RESOLVED_TYPE = Map.class;

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.MAP;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            return iterator.next().resolveProperty(RESOLVED_TYPE, context, annotations, iterator);
        }
        return null;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            return iterator.next().resolve(RESOLVED_TYPE, context, iterator);
        }
        return null;
    }
}
