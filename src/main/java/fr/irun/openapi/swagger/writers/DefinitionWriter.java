package fr.irun.openapi.swagger.writers;

import fr.irun.openapi.swagger.readers.SecurityParser;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public final class DefinitionWriter {
    private final OpenAPI output;

    public Optional<OpenAPIDefinition> write(Class<?> clazz) {
        OpenAPIDefinition openAPIDefinition = ReflectionUtils.getAnnotation(clazz, OpenAPIDefinition.class);

        if (openAPIDefinition == null) {
            return Optional.empty();
        }

        AnnotationsUtils.getInfo(openAPIDefinition.info()).ifPresent(output::setInfo);

        // OpenApiDefinition security requirements
        SecurityParser.getSecurityRequirements(openAPIDefinition.security())
                .ifPresent(output::setSecurity);
        //
        // OpenApiDefinition external docs
        AnnotationsUtils
                .getExternalDocumentation(openAPIDefinition.externalDocs())
                .ifPresent(output::setExternalDocs);

        // OpenApiDefinition tags
        AnnotationsUtils.getTags(openAPIDefinition.tags(), false)
                .ifPresent(tags -> output.getTags().addAll(tags));

        // OpenApiDefinition servers
        AnnotationsUtils.getServers(openAPIDefinition.servers()).ifPresent(output::setServers);

        // OpenApiDefinition extensions
        if (openAPIDefinition.extensions().length > 0) {
            output.setExtensions(AnnotationsUtils
                    .getExtensions(openAPIDefinition.extensions()));
        }

        return Optional.of(openAPIDefinition);
    }
}
