package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized ModelResolver for a type wrapping a generic array (e.g. Flux).
 */
public class GenericArrayModelResolver implements RocketModelResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericArrayModelResolver.class);

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.WRAP_GENERIC_ARRAY;
    }


    @Override
    public Property resolveProperty(Type fluxType, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            // Property of type MyWrapper<T> converted to T[]
            final Type innerFluxType = ModelConversionUtils.extractGenericFirstInnerType(fluxType);
            final ModelConverter converter = iterator.next();
            LOGGER.trace("Strategy {}: resolve property type {} with {}", getResolutionStrategy(), innerFluxType, converter.getClass());
            final Property baseProperty = converter.resolveProperty(innerFluxType, context, annotations, iterator);
            return new ArrayProperty(baseProperty);
        }
        return null;
    }

    @Override
    public Model resolve(Type fluxType, ModelConverterContext modelConvertcontextrContext, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            final Type innerFluxType = ModelConversionUtils.extractGenericFirstInnerType(fluxType);
            final ModelConverter converter = iterator.next();
            LOGGER.trace("Strategy {}: resolve model type {} with {}", getResolutionStrategy(), innerFluxType, converter.getClass());
            return converter.resolve(innerFluxType, modelConvertcontextrContext, iterator);
        }
        return null;
    }
}
