package fr.irun.openapi.swagger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class SpringOpenApiScanner implements OpenApiScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringOpenApiScanner.class);

    private OpenAPIConfiguration openApiConfiguration;

    @Override
    public void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        this.openApiConfiguration = openApiConfiguration;
    }

    @Override
    public Set<Class<?>> classes() {
        LOGGER.debug("Scan classes for Spring application...");
        Objects.requireNonNull(openApiConfiguration, "OpenAPI configuration is mandatory !");

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        return Optional.ofNullable(openApiConfiguration.getResourcePackages()).orElse(ImmutableSet.of("")).stream()
                .flatMap(p -> scanner.findCandidateComponents(p).stream())
                .map(b -> {
                    try {
                        LOGGER.debug("find class: {}", b.getBeanClassName());
                        return Class.forName(b.getBeanClassName());
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("Unable to get class '{}', {}: {}", b.getBeanClassName(), e.getClass(), e.getLocalizedMessage());
                        LOGGER.debug("STACKTRACE", e);
                        return null;
                    }
                }).filter(((Predicate<Class<?>>) Objects::isNull).negate())
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Map<String, Object> resources() {
        return ImmutableMap.of();
    }
}
