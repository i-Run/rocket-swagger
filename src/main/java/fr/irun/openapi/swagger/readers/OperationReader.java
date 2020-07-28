package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.irun.openapi.swagger.utils.OperationIdProvider;
import fr.irun.openapi.swagger.utils.ReaderUtils;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.Map;

@AllArgsConstructor
public final class OperationReader {
    private final OperationIdProvider operationIdProvider;
    private final GlobalElementReader globalElementReader;
    private final OpenAPIExtension extension;

    public Operation read(io.swagger.v3.oas.annotations.Operation apiOperation,
                          RequestMapping methodConsumes, RequestMapping classConsumes, JsonView jsonViewAnnotation) {
        final Components components = globalElementReader.getComponents();
        final io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
        if (StringUtils.isNotBlank(apiOperation.summary())) {
            operation.setSummary(apiOperation.summary());
        }
        if (StringUtils.isNotBlank(apiOperation.description())) {
            operation.setDescription(apiOperation.description());
        }
        if (StringUtils.isNotBlank(apiOperation.operationId())) {
            operation.setOperationId(
                    operationIdProvider.provideOperationId(apiOperation.operationId()));
        }
        if (apiOperation.deprecated()) {
            operation.setDeprecated(apiOperation.deprecated());
        }

        ReaderUtils.getStringListFromStringArray(apiOperation.tags()).ifPresent(tags ->
                tags.stream()
                        .distinct()
                        .forEach(operation::addTagsItem));

        AnnotationsUtils.getExternalDocumentation(apiOperation.externalDocs())
                .ifPresent(operation::setExternalDocs);

        OperationParser.getApiResponses(apiOperation.responses(), null, null, components, jsonViewAnnotation)
                .ifPresent(operation::setResponses);
        AnnotationsUtils.getServers(apiOperation.servers())
                .ifPresent(servers -> servers.forEach(operation::addServersItem));

        for (io.swagger.v3.oas.annotations.Parameter parameter : apiOperation.parameters()) {
            ResolvedParameter resolvedParameter = extension.extractParameters(
                    Collections.singletonList(parameter), ParameterProcessor.getParameterType(parameter),
                    Collections.emptySet(), components, classConsumes, methodConsumes,
                    true, jsonViewAnnotation, null);

            resolvedParameter.getParameters().forEach(operation::addParametersItem);
        }

        SecurityParser.getSecurityRequirements(apiOperation.security())
                .ifPresent(securityRequirements ->
                        securityRequirements.stream()
                                .distinct()
                                .forEach(operation::addSecurityItem));

        OperationParser.getRequestBody(apiOperation.requestBody(), classConsumes, methodConsumes, components, jsonViewAnnotation)
                .ifPresent(operation::setRequestBody);

        if (apiOperation.extensions().length > 0) {
            Map<String, Object> extensions = AnnotationsUtils.getExtensions(apiOperation.extensions());
            for (String ext : extensions.keySet()) {
                operation.addExtension(ext, extensions.get(ext));
            }
        }

        return operation;
    }
}
