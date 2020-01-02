package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Model resolvers for datetime types: {@link java.time.Instant},{@link java.time.LocalDateTime},{@link java.util.Date},{@link java.sql.Date}.
 * Used because swagger does not convert the dates with correct format.
 */
public class DateTimeModelResolver implements RocketModelResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeModelResolver.class);

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.DATE_TIME;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        LOGGER.trace("Strategy {} - resolve property for type {}", getResolutionStrategy(), type);
        return new DateTimeProperty();
    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            final ModelConverter converter = iterator.next();
            LOGGER.trace("Strategy {} - resolve model for type {} with converter {}", getResolutionStrategy(), type, converter.getClass());
            return converter.resolve(type, context, iterator);
        }
        return null;
    }
}
