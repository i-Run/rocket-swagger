package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized ModelResolver for a type wrapping a generic array (e.g. Flux).
 */
@Getter
public class GenericArrayModelResolver implements RocketModelResolver {

    private final ModelConverter baseConverter;

    /**
     * Construct with base converter.
     *
     * @param baseConverter Base converter to use to resolve the inner type.
     */
    public GenericArrayModelResolver(ModelConverter baseConverter) {
        this.baseConverter = baseConverter;
    }

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.WRAP_GENERIC_ARRAY;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        // Property of type MyWrapper<T> converted to T[]
        return ModelConversionUtils.extractGenericFirstInnerType(type)
                .map(t -> baseConverter.resolveProperty(t, context, annotations, iterator))
                .map(ArrayProperty::new)
                .orElse(null);
    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> iterator) {
        return ModelConversionUtils.extractGenericFirstInnerType(type)
                .map(t -> baseConverter.resolve(t, context, iterator))
                .orElse(null);
    }
}
