package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import fr.irun.openapi.swagger.exceptions.RocketSwaggerException;
import fr.irun.openapi.swagger.resolver.DateTimeModelResolver;
import fr.irun.openapi.swagger.resolver.DefaultModelResolver;
import fr.irun.openapi.swagger.resolver.GenericArrayModelResolver;
import fr.irun.openapi.swagger.resolver.GenericModelResolver;
import fr.irun.openapi.swagger.resolver.MapModelResolver;
import fr.irun.openapi.swagger.resolver.RocketModelResolver;
import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.converter.ModelConverters;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base model converter for the Rocket modules.
 */
public class RocketModelConverter implements ModelConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketModelConverter.class);

    private final ImmutableMap<ResolutionStrategy, RocketModelResolver> resolversByStrategy;

    /**
     * Default constructor used by swagger-maven-plugin.
     */
    public RocketModelConverter() {
        this(Json.mapper());
    }

    /**
     * Constructor used to customize Jackson configuration.
     *
     * @param objectMapper Mapper from Jackson configuration.
     */
    public RocketModelConverter(ObjectMapper objectMapper) {
        registerJacksonConverters(objectMapper);

        this.resolversByStrategy = ImmutableMap.copyOf(
                Stream.of(
                        new DefaultModelResolver(),
                        new DateTimeModelResolver(),
                        new MapModelResolver(),
                        new GenericModelResolver(this),
                        new GenericArrayModelResolver(this)
                ).collect(Collectors.toMap(RocketModelResolver::getResolutionStrategy, Functions.identity()))
        );
    }

    @VisibleForTesting
    RocketModelConverter(ImmutableMap<ResolutionStrategy, RocketModelResolver> resolvers) {
        this.resolversByStrategy = resolvers;
    }

    @VisibleForTesting
    ImmutableMap<ResolutionStrategy, RocketModelResolver> getResolversByStrategy() {
        return resolversByStrategy;
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
        final ResolutionStrategy resolutionStrategy = ModelConversionUtils.getResolutionStrategy(type);
        final RocketModelResolver resolver = Optional.ofNullable(resolversByStrategy.get(resolutionStrategy))
                .orElseThrow(() -> new RocketSwaggerException("Unable to find model resolver for model type: " + resolutionStrategy));
        LOGGER.trace("Resolve type '{}' with strategy '{}' and converter {}", type, resolutionStrategy, resolver.getClass());
        return resolver;
    }

    private static void registerJacksonConverters(ObjectMapper objectMapper) {
        // Required to use the expected object mapper for the conversion
        ModelConverters.getInstance().addConverter(new ModelResolver(objectMapper));
    }

}
