package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import lombok.Getter;

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
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        return ModelConversionUtils.extractGenericFirstInnerType(type)
                .map(t -> baseConverter.resolve(t, context, chain))
                .orElse(null);
    }
}
