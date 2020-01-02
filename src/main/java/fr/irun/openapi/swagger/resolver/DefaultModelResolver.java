package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Default Model resolver.
 */
public class DefaultModelResolver implements RocketModelResolver {

    private final ModelConverter modelConverter;

    /**
     * Constructor.
     *
     * @param modelConverter The base used Model converter.
     */
    public DefaultModelResolver(ModelConverter modelConverter) {
        this.modelConverter = modelConverter;
    }

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return ResolutionStrategy.DEFAULT;
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        return modelConverter.resolveProperty(type, context, annotations, iterator);
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        return modelConverter.resolve(type, modelConverterContext, iterator);
    }
}
