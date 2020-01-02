package fr.irun.openapi.swagger.resolver;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

public class MapModelResolver implements RocketModelResolver {

    @VisibleForTesting
    static final Class<?> RESOLVED_TYPE = ImmutableMap.<String, Object>of().getClass();

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
