package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized ModelResolver for a type wrapping a generic array (e.g. Flux).
 */
public class GenericArrayModelResolver implements RocketModelResolver {

    private final ModelConverter modelConverter;

    /**
     * Constructor.
     *
     * @param modelConverter the base model converter.
     */
    public GenericArrayModelResolver(ModelConverter modelConverter) {
        this.modelConverter = modelConverter;
    }

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.WRAP_GENERIC_ARRAY;
    }


    @Override
    public Property resolveProperty(Type fluxType,
                                    ModelConverterContext modelConverterContext,
                                    Annotation[] annotations,
                                    Iterator<ModelConverter> iterator) {

        // Property of type MyWrapper<T> converted to T[]
        final Type innerFluxType = ModelConversionUtils.extractGenericFirstInnerType(fluxType);
        final Property baseProperty = modelConverter.resolveProperty(innerFluxType, modelConverterContext, annotations, iterator);
        return new ArrayProperty(baseProperty);
    }

    @Override
    public Model resolve(Type fluxType, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        final Type innerFluxType = ModelConversionUtils.extractGenericFirstInnerType(fluxType);
        return modelConverter.resolve(innerFluxType, modelConverterContext, iterator);
    }
}
