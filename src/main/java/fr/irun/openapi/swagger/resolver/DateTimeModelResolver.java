package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Iterator;

/**
 * Model resolvers for datetime types: {@link java.time.Instant},{@link java.time.LocalDateTime},{@link java.util.Date},{@link java.sql.Date}.
 * Used because swagger does not convert the dates with correct format.
 */
public class DateTimeModelResolver implements RocketModelResolver {

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.DATE_TIME;
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        }
        return null;
    }
}
