package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
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
import io.swagger.util.Json;

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

    private final Map<ModelEnum, RocketModelResolver> resolversMappedByType;

    /**
     * Default constructor used by swagger-maven-plugin.
     */
    public RocketModelConverter() {
        this(Json.mapper());
    }

    /**
     * Constructor used to customize Jackson configuration.
     * @param objectMapper Mapper from Jackson configuration.
     */
    public RocketModelConverter(ObjectMapper objectMapper) {
        final ModelConverter baseConverter = new BaseModelConverter(objectMapper);

        this.resolversMappedByType = ImmutableMap.copyOf(
                Stream.of(
                        new StandardModelResolver(baseConverter),
                        new MonoModelResolver(baseConverter),
                        new FluxModelResolver(baseConverter)
                ).collect(Collectors.toMap(RocketModelResolver::getModelType, Functions.identity()))
        );
    }


    @VisibleForTesting
    RocketModelConverter(Collection<RocketModelResolver> consolidations) {
        this.resolversMappedByType = ImmutableMap.copyOf(
                consolidations.stream()
                        .collect(Collectors.toMap(RocketModelResolver::getModelType, Functions.identity()))
        );
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
        return Optional.ofNullable(resolversMappedByType.get(modelType))
                .orElseThrow(() -> new RocketSwaggerException("Unable to find model resolver for model type: " + modelType));
    }

}
