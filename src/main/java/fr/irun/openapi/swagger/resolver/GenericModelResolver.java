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

    private final ModelConverter modelConverter;

    /**
     * Constructor.
     *
     * @param modelConverter Base model converter used.
     */
    public GenericModelResolver(ModelConverter modelConverter) {
        this.modelConverter = modelConverter;
    }


    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.WRAP_GENERIC;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        // Property of type MyWrapper<T> converted to T
        final Type innerMonoType = ModelConversionUtils.extractGenericFirstInnerType(type);
        return modelConverter.resolveProperty(innerMonoType, context, annotations, iterator);
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        final Type innerMonoType = ModelConversionUtils.extractGenericFirstInnerType(type);
        return modelConverter.resolve(innerMonoType, modelConverterContext, iterator);
    }
}
