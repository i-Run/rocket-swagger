package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized Model resolver for a type wrapping another (e.g. Mono or ResponseEntity).
 */
public class GenericModelResolver implements RocketModelResolver {

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.WRAP_GENERIC;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            final ModelConverter converter = iterator.next();
            // Property of type MyWrapper<T> converted to T
            return ModelConversionUtils.extractGenericFirstInnerType(type)
                    .map(t -> converter.resolveProperty(t, context, annotations, iterator))
                    .orElse(null);
        }
        return null;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> iterator) {
        if (iterator.hasNext()) {
            final ModelConverter converter = iterator.next();
            return ModelConversionUtils.extractGenericFirstInnerType(type)
                    .map(t -> converter.resolve(t, context, iterator))
                    .orElse(null);
        }
        return null;
    }
}
