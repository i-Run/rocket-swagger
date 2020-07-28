package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Iterator;
import java.util.Map;

/**
 * Resolver to resolve a type as a Map.
 */
public class MapModelResolver implements RocketModelResolver {

    private static final AnnotatedType RESOLVED_TYPE = new AnnotatedType(Map.class);

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.MAP;
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            return chain.next().resolve(RESOLVED_TYPE, context, chain);
        }
        return null;
    }
}
