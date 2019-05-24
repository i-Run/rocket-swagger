package fr.irun.openapi.swagger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import fr.irun.openapi.swagger.converter.BaseModelConverter;
import fr.irun.openapi.swagger.exceptions.RocketSwaggerException;
import fr.irun.openapi.swagger.resolver.FluxModelResolver;
import fr.irun.openapi.swagger.resolver.MonoModelResolver;
import fr.irun.openapi.swagger.resolver.RocketModelResolver;
import fr.irun.openapi.swagger.resolver.StandardModelResolver;
import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base model converter for the Rocket modules.
 */
public class RocketModelConverter implements ModelConverter {

    private final Map<ModelEnum, RocketModelResolver> consolidationMap;


    /**
     * Default constructor used by swagger-maven-plugin.
     */
    public RocketModelConverter() {
        final ModelConverter baseConverter = new BaseModelConverter();
        this.consolidationMap = Stream.of(
                new StandardModelResolver(baseConverter),
                new MonoModelResolver(baseConverter),
                new FluxModelResolver(baseConverter)
        ).collect(Collectors.toMap(RocketModelResolver::getModelType, Functions.identity()));
    }


    @VisibleForTesting
    RocketModelConverter(Collection<RocketModelResolver> consolidations) {
        this.consolidationMap = consolidations.stream()
                .collect(Collectors.toMap(RocketModelResolver::getModelType, Functions.identity()));
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        return getResolverForType(type).resolveProperty(type, context, annotations, iterator);
    }


    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> iterator) {
        return getResolverForType(type).resolve(type, context, iterator);
    }

    @Nonnull
    private RocketModelResolver getResolverForType(Type type) {
        final ModelEnum modelType = ModelConversionUtils.computeModelType(type);
        return Optional.ofNullable(consolidationMap.get(modelType))
                .orElseThrow(() -> new RocketSwaggerException("Unable to find model resolver for model type: " + modelType));
    }

}
