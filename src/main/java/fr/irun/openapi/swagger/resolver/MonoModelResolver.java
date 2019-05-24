package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Customized Model resolver for Mono.
 */
public class MonoModelResolver implements RocketModelResolver {

    private final ModelConverter modelConverter;

    /**
     * Constructor.
     *
     * @param modelConverter Base model converter used.
     */
    public MonoModelResolver(ModelConverter modelConverter) {
        this.modelConverter = modelConverter;
    }


    @Override
    public ModelEnum getModelType() {
        return ModelEnum.MONO;
    }

    @Override
    public Property resolveProperty(Type monoType, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        // Property of type Mono<T> converted to T
        final Type innerMonoType = ModelConversionUtils.extractGenericFirstInnerType(monoType);
        return modelConverter.resolveProperty(innerMonoType, context, annotations, iterator);
    }

    @Override
    public Model resolve(Type monoType, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        final Type innerMonoType = ModelConversionUtils.extractGenericFirstInnerType(monoType);
        return modelConverter.resolve(innerMonoType, modelConverterContext, iterator);
    }
}
