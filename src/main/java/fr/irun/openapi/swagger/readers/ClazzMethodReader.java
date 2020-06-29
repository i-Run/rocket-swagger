package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.google.common.collect.Maps;
import fr.irun.openapi.swagger.utils.ReaderUtils;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.PathUtils;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public final class ClazzMethodReader {
    private final boolean isReadAllResources;
    private final Collection<String> ignoredRoutes;
    private final CallbackReader callbackReader;

    private final RequestMapping clazzRequestMappingAnnotation;
    private final String parentPath;

    public void read(Method method) {
        if (isOperationHidden(method)
                || ReflectionUtils.isOverriddenMethod(method, method.getDeclaringClass())) {
            return;
        }
        RequestMapping methodRequestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);

        String operationPath = ReaderUtils.getPath(clazzRequestMappingAnnotation, methodRequestMapping, parentPath, false);

        // skip if path is the same as parent, e.g. for @ApplicationPath annotated application
        // extending resource config.
        if (ignoreOperationPath(operationPath, parentPath)) {
            return;
        }

        Map<String, String> regexMap = Maps.newLinkedHashMap();
        operationPath = PathUtils.parsePath(operationPath, regexMap);
        if (operationPath == null) {
            return;
        }
        if (ReaderUtils.isIgnored(operationPath, ignoredRoutes)) {
            return;
        }

        String httpMethod = ReaderUtils.extractOperationMethod(method, OpenAPIExtensions.chain());
        if (StringUtils.isBlank(httpMethod)) {
            return;
        }

        Operation apiOperation = ReflectionUtils.getAnnotation(method, Operation.class);
        JsonView jsonViewAnnotation;
        JsonView jsonViewAnnotationForRequestBody;
        if (apiOperation != null && apiOperation.ignoreJsonView()) {
            jsonViewAnnotation = null;
            jsonViewAnnotationForRequestBody = null;
        } else {
            jsonViewAnnotation = ReflectionUtils.getAnnotation(method, JsonView.class);
                    /* If one and only one exists, use the @JsonView annotation from the method parameter annotated
                       with @RequestBody. Otherwise fall back to the @JsonView annotation for the method itself. */
            jsonViewAnnotationForRequestBody = (JsonView) Arrays.stream(ReflectionUtils.getParameterAnnotations(method))
                    .filter(arr -> Arrays.stream(arr).anyMatch(annotation ->
                            annotation.annotationType()
                                    .equals(io.swagger.v3.oas.annotations.parameters.RequestBody.class)
                    ))
                    .flatMap(Arrays::stream)
                    .filter(annotation ->
                            annotation.annotationType().equals(JsonView.class))
                    .reduce((a, b) -> jsonViewAnnotation)
                    .orElse(jsonViewAnnotation);
        }

//            Operation operation = parseMethod(
//                    method, globalParameters, methodRequestMapping, apiRequestMapping, classSecurityRequirements,
//                    classExternalDocumentation, classTags, classServers, isSubresource, parentRequestBody,
//                    parentResponses, jsonViewAnnotation, classResponses, annotatedMethod);
//
//            if (operation != null) {
//
//                List<Parameter> operationParameters = new ArrayList<>();
//                List<Parameter> formParameters = new ArrayList<>();
//                for (java.lang.reflect.Parameter parameter : method.getParameters()) {
//                    Type parameterizedType = parameter.getParameterizedType();
//
//                    final Type type = TypeFactory.defaultInstance().constructType(parameterizedType, cls);
//                    ImmutableSet<Class<? extends Annotation>> annotationTypes = ImmutableSet.of(RequestParam.class, PathVariable.class, MatrixVariable.class, RequestHeader.class, CookieValue.class);
//                    Set<Annotation> annotations = annotationTypes.stream()
//                            .map(at -> AnnotatedElementUtils.findMergedAnnotation(parameter, at))
//                            .filter(Objects::nonNull)
//                            .map(a -> {
//                                Object value = AnnotationUtils.getValue(a, "value");
//                                if (value == null || Strings.isNullOrEmpty(value.toString())) {
//                                    return AnnotationUtils.synthesizeAnnotation(ImmutableMap.of("value", parameter.getName()), a.annotationType(), parameter);
//                                } else {
//                                    return a;
//                                }
//                            }).collect(Collectors.toSet());
//
//                    io.swagger.v3.oas.annotations.Parameter paramAnnotation = AnnotationUtils.getAnnotation(parameter, io.swagger.v3.oas.annotations.Parameter.class);
//                    if (paramAnnotation != null) {
//                        annotations.add(paramAnnotation);
//                    }
//
//                    Type paramType = ParameterProcessor.getParameterType(paramAnnotation, true);
//                    if (paramType == null) {
//                        paramType = type;
//                    } else {
//                        if (!(paramType instanceof Class)) {
//                            paramType = type;
//                        }
//                    }
//                    ResolvedParameter resolvedParameter = getParameters(paramType, ImmutableList.copyOf(annotations), operation, apiRequestMapping, methodRequestMapping, jsonViewAnnotation);
//                    operationParameters.addAll(resolvedParameter.parameters);
//                    // collect params to use together as request Body
//                    formParameters.addAll(resolvedParameter.formParameters);
//                    if (resolvedParameter.requestBody != null) {
//                        processRequestBody(
//                                resolvedParameter.requestBody, operation, methodRequestMapping, apiRequestMapping,
//                                operationParameters, parameter.getDeclaredAnnotations(), type, jsonViewAnnotationForRequestBody);
//                    }
//                }
//
//                // if we have form parameters, need to merge them into single schema and use as request body..
//                if (formParameters.size() > 0) {
//                    Schema<?> mergedSchema = new ObjectSchema();
//                    for (Parameter formParam : formParameters) {
//                        mergedSchema.addProperties(formParam.getName(), formParam.getSchema());
//                        if (null != formParam.getRequired() && formParam.getRequired()) {
//                            mergedSchema.addRequiredItem(formParam.getName());
//                        }
//                    }
//                    Parameter merged = new Parameter().schema(mergedSchema);
//                    processRequestBody(
//                            merged, operation, methodRequestMapping, apiRequestMapping, operationParameters,
//                            new Annotation[0], null, jsonViewAnnotationForRequestBody);
//
//                }
//                if (operationParameters.size() > 0) {
//                    for (Parameter operationParameter : operationParameters) {
//                        operation.addParametersItem(operationParameter);
//                    }
//                }
//
//                // if subresource, merge parent parameters
//                if (parentParameters != null) {
//                    for (Parameter parentParameter : parentParameters) {
//                        operation.addParametersItem(parentParameter);
//                    }
//                }
//
//                if (subResource != null && !scannedResources.contains(subResource)) {
//                    scannedResources.add(subResource);
//                    read(subResource, operationPath, httpMethod, true, operation.getRequestBody(),
//                            operation.getResponses(), classTags, operation.getParameters(), scannedResources);
//                    // remove the sub resource so that it can visit it later in another path
//                    // but we have a room for optimization in the future to reuse the scanned result
//                    // by caching the scanned resources in the reader instance to avoid actual scanning
//                    // the the resources again
//                    scannedResources.remove(subResource);
//                    // don't proceed with root resource operation, as it's handled by subresource
//                    continue;
//                }
//
//                final Iterator<OpenAPIExtension> chain = OpenAPIExtensions.chain();
//                if (chain.hasNext()) {
//                    final OpenAPIExtension extension = chain.next();
//                    extension.decorateOperation(operation, method, chain);
//                }
//
//                PathItem pathItemObject;
//                if (openAPI.getPaths() != null && openAPI.getPaths().get(operationPath) != null) {
//                    pathItemObject = openAPI.getPaths().get(operationPath);
//                } else {
//                    pathItemObject = new PathItem();
//                }
//
//                if (StringUtils.isBlank(httpMethod)) {
//                    continue;
//                }
//                setPathItemOperation(pathItemObject, httpMethod, operation);
//
//                paths.addPathItem(operationPath, pathItemObject);
//                if (openAPI.getPaths() != null) {
//                    this.paths.putAll(openAPI.getPaths());
//                }
//
//                openAPI.setPaths(this.paths);
//
//            }
    }

    private io.swagger.v3.oas.models.Operation parseMethod(
            Method method,
            List<Parameter> globalParameters,
            RequestMapping methodConsumes,
            RequestMapping classConsumes,
            List<SecurityRequirement> classSecurityRequirements,
            Optional<ExternalDocumentation> classExternalDocs,
            Set<String> classTags,
            List<io.swagger.v3.oas.models.servers.Server> classServers,
            boolean isSubresource,
            RequestBody parentRequestBody,
            ApiResponses parentResponses,
            JsonView jsonViewAnnotation,
            io.swagger.v3.oas.annotations.responses.ApiResponse[] classResponses,
            AnnotatedMethod annotatedMethod) {

        io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();

        io.swagger.v3.oas.annotations.Operation apiOperation = ReflectionUtils.getAnnotation(method, io.swagger.v3.oas.annotations.Operation.class);

        List<io.swagger.v3.oas.annotations.security.SecurityRequirement> apiSecurity = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.security.SecurityRequirement.class);
        List<io.swagger.v3.oas.annotations.callbacks.Callback> apiCallbacks = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.callbacks.Callback.class);
        List<Server> apiServers = ReflectionUtils.getRepeatableAnnotations(method, Server.class);
        List<io.swagger.v3.oas.annotations.tags.Tag> apiTags = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.tags.Tag.class);
        List<io.swagger.v3.oas.annotations.Parameter> apiParameters = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.Parameter.class);
        List<io.swagger.v3.oas.annotations.responses.ApiResponse> apiResponses = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.responses.ApiResponse.class);
        io.swagger.v3.oas.annotations.parameters.RequestBody apiRequestBody =
                ReflectionUtils.getAnnotation(method, io.swagger.v3.oas.annotations.parameters.RequestBody.class);

        io.swagger.v3.oas.annotations.ExternalDocumentation apiExternalDocumentation = ReflectionUtils.getAnnotation(method, io.swagger.v3.oas.annotations.ExternalDocumentation.class);

        // callbacks
        Map<String, Callback> callbacks = Maps.newLinkedHashMap();

        if (apiCallbacks != null) {
            for (io.swagger.v3.oas.annotations.callbacks.Callback methodCallback : apiCallbacks) {
                Map<String, Callback> currentCallbacks = callbackReader.readCallbacks(methodCallback, methodConsumes, classConsumes, jsonViewAnnotation);
                callbacks.putAll(currentCallbacks);
            }
        }
        if (callbacks.size() > 0) {
            operation.setCallbacks(callbacks);
        }

        // security
        classSecurityRequirements.forEach(operation::addSecurityItem);
        if (apiSecurity != null) {
            Optional<List<SecurityRequirement>> requirementsObject = SecurityParser.getSecurityRequirements(apiSecurity.toArray(new io.swagger.v3.oas.annotations.security.SecurityRequirement[apiSecurity.size()]));
            if (requirementsObject.isPresent()) {
                requirementsObject.get().stream()
                        .filter(r -> operation.getSecurity() == null || !operation.getSecurity().contains(r))
                        .forEach(operation::addSecurityItem);
            }
        }

        // servers
        if (classServers != null) {
            classServers.forEach(operation::addServersItem);
        }

        if (apiServers != null) {
            AnnotationsUtils.getServers(apiServers.toArray(new Server[apiServers.size()])).ifPresent(servers -> servers.forEach(operation::addServersItem));
        }

        // external docs
        AnnotationsUtils.getExternalDocumentation(apiExternalDocumentation).ifPresent(operation::setExternalDocs);

//        // method tags
//        if (apiTags != null) {
//            apiTags.stream()
//                    .filter(t -> operation.getTags() == null || (operation.getTags() != null && !operation.getTags().contains(t.name())))
//                    .map(io.swagger.v3.oas.annotations.tags.Tag::name)
//                    .forEach(operation::addTagsItem);
//            AnnotationsUtils.getTags(apiTags.toArray(new io.swagger.v3.oas.annotations.tags.Tag[apiTags.size()]), true).ifPresent(tags -> openApiTags.addAll(tags));
//        }
//
//        // parameters
//        if (globalParameters != null) {
//            for (Parameter globalParameter : globalParameters) {
//                operation.addParametersItem(globalParameter);
//            }
//        }
//        if (apiParameters != null) {
//            getParametersListFromAnnotation(
//                    apiParameters.toArray(new io.swagger.v3.oas.annotations.Parameter[apiParameters.size()]),
//                    classConsumes,
//                    methodConsumes,
//                    operation,
//                    jsonViewAnnotation).ifPresent(p -> p.forEach(operation::addParametersItem));
//        }
//
//        // RequestBody in Method
//        if (apiRequestBody != null && operation.getRequestBody() == null) {
//            OperationParser.getRequestBody(apiRequestBody, classConsumes, methodConsumes, components, jsonViewAnnotation).ifPresent(
//                    operation::setRequestBody);
//        }
//
//        // operation id
//        if (StringUtils.isBlank(operation.getOperationId())) {
//            operation.setOperationId(getOperationId(method.getName()));
//        }
//
//        // classResponses
//        if (classResponses != null && classResponses.length > 0) {
//            OperationParser.getApiResponses(
//                    classResponses,
//                    null,
//                    null,
//                    components,
//                    jsonViewAnnotation
//            ).ifPresent(responses -> {
//                if (operation.getResponses() == null) {
//                    operation.setResponses(responses);
//                } else {
//                    responses.forEach(operation.getResponses()::addApiResponse);
//                }
//            });
//        }
//
//        if (apiOperation != null) {
//            setOperationObjectFromApiOperationAnnotation(operation, apiOperation, methodConsumes, classConsumes, jsonViewAnnotation);
//        }
//
//        // apiResponses
//        if (apiResponses != null && apiResponses.size() > 0) {
//            OperationParser.getApiResponses(
//                    apiResponses.toArray(new io.swagger.v3.oas.annotations.responses.ApiResponse[apiResponses.size()]),
//                    null,
//                    null,
//                    components,
//                    jsonViewAnnotation
//            ).ifPresent(responses -> {
//                if (operation.getResponses() == null) {
//                    operation.setResponses(responses);
//                } else {
//                    responses.forEach(operation.getResponses()::addApiResponse);
//                }
//            });
//        }
//
//        // class tags after tags defined as field of @Operation
//        if (classTags != null) {
//            classTags.stream()
//                    .filter(t -> operation.getTags() == null || (operation.getTags() != null && !operation.getTags().contains(t)))
//                    .forEach(operation::addTagsItem);
//        }
//
//        // external docs of class if not defined in annotation of method or as field of Operation annotation
//        if (operation.getExternalDocs() == null) {
//            classExternalDocs.ifPresent(operation::setExternalDocs);
//        }
//
//        // if subresource, merge parent requestBody
//        if (isSubresource && parentRequestBody != null) {
//            if (operation.getRequestBody() == null) {
//                operation.requestBody(parentRequestBody);
//            } else {
//                Content content = operation.getRequestBody().getContent();
//                if (content == null) {
//                    content = parentRequestBody.getContent();
//                    operation.getRequestBody().setContent(content);
//                } else if (parentRequestBody.getContent() != null) {
//                    for (String parentMediaType : parentRequestBody.getContent().keySet()) {
//                        if (content.get(parentMediaType) == null) {
//                            content.addMediaType(parentMediaType, parentRequestBody.getContent().get(parentMediaType));
//                        }
//                    }
//                }
//            }
//        }
//
//        // handle return type, add as response in case.
//        Type returnType = method.getGenericReturnType();
//
//        if (annotatedMethod != null && annotatedMethod.getType() != null) {
//            returnType = annotatedMethod.getType();
//        }
//
//        final Class<?> subResource = getSubResourceWithJaxRsSubresourceLocatorSpecs(method);
//        if (!shouldIgnoreClass(returnType.getTypeName()) && !method.getGenericReturnType().equals(subResource)) {
//            ResolvedSchema resolvedSchema = ModelConverters.getInstance()
//                    .resolveAsResolvedSchema(new AnnotatedType(returnType).resolveAsRef(true).jsonViewAnnotation(jsonViewAnnotation));
//            if (resolvedSchema.schema != null) {
//                Schema returnTypeSchema = resolvedSchema.schema;
//                Content content = new Content();
//                MediaType mediaType = new MediaType().schema(returnTypeSchema);
//                AnnotationsUtils.applyTypes(new String[0], new String[]{DEFAULT_MEDIA_TYPE_VALUE}, content, mediaType);
//                if (operation.getResponses() == null) {
//                    operation.responses(
//                            new ApiResponses().addApiResponse(DEFAULT_RESPONSE_STATUS,
//                                    new ApiResponse().description(DEFAULT_DESCRIPTION).content(content))
//                    );
//                }
//                ApiResponse defaultApiResponse = operation.getResponses().get(DEFAULT_RESPONSE_STATUS);
//                if (defaultApiResponse != null &&
//                        StringUtils.isBlank(defaultApiResponse.get$ref())) {
//                    if (defaultApiResponse.getContent() == null) {
//                        defaultApiResponse.content(content);
//                    } else {
//                        for (String key : defaultApiResponse.getContent().keySet()) {
//                            if (defaultApiResponse.getContent().get(key).getSchema() == null) {
//                                defaultApiResponse.getContent().get(key).setSchema(returnTypeSchema);
//                            }
//                        }
//                    }
//                } else {
//                    operation.getResponses().addApiResponse(DEFAULT_RESPONSE_STATUS,
//                            new ApiResponse().description(DEFAULT_DESCRIPTION).content(content));
//                }
//                Map<String, Schema> schemaMap = resolvedSchema.referencedSchemas;
//                if (schemaMap != null) {
//                    schemaMap.forEach((key, schema) -> components.addSchemas(key, schema));
//                }
//
//            }
//        }
//        if (operation.getResponses() == null || operation.getResponses().isEmpty()) {
//            Content content = new Content();
//            MediaType mediaType = new MediaType();
//            AnnotationsUtils.applyTypes(new String[0], new String[0], content, mediaType);
//
//            ApiResponse apiResponseObject = new ApiResponse().description(DEFAULT_DESCRIPTION).content(content);
//            operation.setResponses(new ApiResponses()._default(apiResponseObject));
//        }

        return operation;
    }

    private boolean isOperationHidden(Method method) {
        Operation apiOperation = ReflectionUtils.getAnnotation(method, Operation.class);
        if (apiOperation != null && apiOperation.hidden()) {
            return true;
        }
        Hidden hidden = method.getAnnotation(Hidden.class);
        if (hidden != null) {
            return true;
        }
        return !isReadAllResources && apiOperation == null;
    }

    private boolean ignoreOperationPath(String path, String parentPath) {

        if (StringUtils.isBlank(path) && StringUtils.isBlank(parentPath)) {
            return true;
        } else if (StringUtils.isNotBlank(path) && StringUtils.isBlank(parentPath)) {
            return false;
        } else if (StringUtils.isBlank(path) && StringUtils.isNotBlank(parentPath)) {
            return false;
        }
        if (parentPath != null && !"".equals(parentPath) && !"/".equals(parentPath)) {
            if (!parentPath.startsWith("/")) {
                parentPath = "/" + parentPath;
            }
            if (parentPath.endsWith("/")) {
                parentPath = parentPath.substring(0, parentPath.length() - 1);
            }
        }
        if (path != null && !"".equals(path) && !"/".equals(path)) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path.equals(parentPath);
    }
}
