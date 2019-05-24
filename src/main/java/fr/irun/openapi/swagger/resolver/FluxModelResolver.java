package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized ModelResolver for Flux.
 */
public class FluxModelResolver implements RocketModelResolver {

    private final ModelConverter modelConverter;

    /**
     * Constructor.
     *
     * @param modelConverter the base model converter.
     */
    public FluxModelResolver(ModelConverter modelConverter) {
        this.modelConverter = modelConverter;
    }

    @Override
    public ModelEnum getModelType() {
        return ModelEnum.FLUX;
    }


    @Override
    public Property resolveProperty(Type fluxType,
                                    ModelConverterContext modelConverterContext,
                                    Annotation[] annotations,
                                    Iterator<ModelConverter> iterator) {

        // Property of type Flux<T> converted to T[]
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
