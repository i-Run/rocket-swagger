package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.irun.openapi.swagger.utils.OpenApiMethod;
import fr.irun.openapi.swagger.utils.OperationIdProvider;
import fr.irun.openapi.swagger.utils.ReaderUtils;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public final class CallbackReader {
    private final OperationIdProvider operationIdProvider;
    private final Components components;
    private final OpenAPIExtension extension;

    public Map<String, Callback> readCallbacks(
            io.swagger.v3.oas.annotations.callbacks.Callback apiCallback,
            RequestMapping methodConsumes,
            RequestMapping classConsumes,
            JsonView jsonViewAnnotation) {
        Map<String, Callback> callbackMap = new HashMap<>();
        if (apiCallback == null) {
            return callbackMap;
        }

        Callback callbackObject = new Callback();
        if (StringUtils.isNotBlank(apiCallback.ref())) {
            callbackObject.set$ref(apiCallback.ref());
            callbackMap.put(apiCallback.name(), callbackObject);
            return callbackMap;
        }
        PathItem pathItemObject = new PathItem();
        for (io.swagger.v3.oas.annotations.Operation callbackOperation : apiCallback.operation()) {
            Operation callbackOperationModel = readOperation(callbackOperation, methodConsumes, classConsumes, jsonViewAnnotation);
            pathItemObject = OpenApiMethod.fromName(callbackOperation.method())
                    .pathItemSetter.apply(pathItemObject, callbackOperationModel);
        }

        callbackObject.addPathItem(apiCallback.callbackUrlExpression(), pathItemObject);
        callbackMap.put(apiCallback.name(), callbackObject);

        return callbackMap;
    }

    private Operation readOperation(io.swagger.v3.oas.annotations.Operation apiOperation,
                                    RequestMapping methodConsumes, RequestMapping classConsumes, JsonView jsonViewAnnotation) {
        final io.swagger.v3.oas.models.Operation newCallbackOperation = new io.swagger.v3.oas.models.Operation();
        if (StringUtils.isNotBlank(apiOperation.summary())) {
            newCallbackOperation.setSummary(apiOperation.summary());
        }
        if (StringUtils.isNotBlank(apiOperation.description())) {
            newCallbackOperation.setDescription(apiOperation.description());
        }
        if (StringUtils.isNotBlank(apiOperation.operationId())) {
            newCallbackOperation.setOperationId(
                    operationIdProvider.provideOperationId(apiOperation.operationId()));
        }
        if (apiOperation.deprecated()) {
            newCallbackOperation.setDeprecated(apiOperation.deprecated());
        }

        ReaderUtils.getStringListFromStringArray(apiOperation.tags()).ifPresent(tags ->
                tags.stream()
                        .distinct()
                        .forEach(newCallbackOperation::addTagsItem));

        AnnotationsUtils.getExternalDocumentation(apiOperation.externalDocs())
                .ifPresent(newCallbackOperation::setExternalDocs);

        OperationParser.getApiResponses(apiOperation.responses(), null, null, components, jsonViewAnnotation)
                .ifPresent(newCallbackOperation::setResponses);
        AnnotationsUtils.getServers(apiOperation.servers())
                .ifPresent(servers -> servers.forEach(newCallbackOperation::addServersItem));

        for (io.swagger.v3.oas.annotations.Parameter parameter : apiOperation.parameters()) {
            ResolvedParameter resolvedParameter = extension.extractParameters(
                    Collections.singletonList(parameter), ParameterProcessor.getParameterType(parameter),
                    Collections.emptySet(), components, classConsumes, methodConsumes,
                    true, jsonViewAnnotation, null);

            resolvedParameter.parameters.forEach(newCallbackOperation::addParametersItem);
        }

        SecurityParser.getSecurityRequirements(apiOperation.security())
                .ifPresent(securityRequirements ->
                        securityRequirements.stream()
                                .distinct()
                                .forEach(newCallbackOperation::addSecurityItem));

        OperationParser.getRequestBody(apiOperation.requestBody(), classConsumes, methodConsumes, components, jsonViewAnnotation)
                .ifPresent(newCallbackOperation::setRequestBody);

        if (apiOperation.extensions().length > 0) {
            Map<String, Object> extensions = AnnotationsUtils.getExtensions(apiOperation.extensions());
            for (String ext : extensions.keySet()) {
                newCallbackOperation.addExtension(ext, extensions.get(ext));
            }
        }

        return newCallbackOperation;
    }

}
