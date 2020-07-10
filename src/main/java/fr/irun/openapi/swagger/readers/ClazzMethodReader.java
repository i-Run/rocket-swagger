package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import fr.irun.openapi.swagger.utils.OpenApiHttpMethod;
import fr.irun.openapi.swagger.utils.OperationIdProvider;
import fr.irun.openapi.swagger.utils.ReaderUtils;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.core.util.PathUtils;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class ClazzMethodReader {
    public static final String DEFAULT_MEDIA_TYPE_VALUE = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
    public static final ImmutableSet<Class<? extends Annotation>> ANNOTATION_TYPES = ImmutableSet.of(
            RequestParam.class, PathVariable.class, MatrixVariable.class, RequestHeader.class, CookieValue.class);
    public static final String DEFAULT_STATUS = "200";

    private final boolean isReadAllResources;
    private final Collection<String> ignoredRoutes;
    private final GlobalElementReader globalElementReader;
    private final CallbackReader callbackReader;
    private final OperationReader operationReader;
    private final OperationIdProvider operationIdProvider;
    private final OpenAPIExtension extension;

    private final RequestMapping clazzRequestMappingAnnotation;
    private final String parentPath;

    public Optional<Map.Entry<String, PathItem>> read(Method method) {
        if (isOperationHidden(method)
                || ReflectionUtils.isOverriddenMethod(method, method.getDeclaringClass())) {
            return Optional.empty();
        }
        RequestMapping methodRequestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        String operationPath = ReaderUtils.getPath(clazzRequestMappingAnnotation, methodRequestMapping, parentPath, false);

        // skip if path is the same as parent, e.g. for @ApplicationPath annotated application
        // extending resource config.
        if (ignoreOperationPath(operationPath, parentPath)) {
            return Optional.empty();
        }

        Map<String, String> regexMap = Maps.newLinkedHashMap();
        operationPath = PathUtils.parsePath(operationPath, regexMap);
        if (operationPath == null) {
            return Optional.empty();
        }
        if (ReaderUtils.isIgnored(operationPath, ignoredRoutes)) {
            return Optional.empty();
        }

        String httpMethod = ReaderUtils.extractOperationMethod(method, OpenAPIExtensions.chain());
        if (StringUtils.isBlank(httpMethod)) {
            return Optional.empty();
        }

        Operation apiOperation = ReflectionUtils.getAnnotation(method, Operation.class);
        JsonView jsonViewAnnotation;
        JsonView jsonViewAnnotationForRequestBody;
        if (apiOperation != null && apiOperation.ignoreJsonView()) {
            jsonViewAnnotation = null;
            jsonViewAnnotationForRequestBody = null;
        } else {
            jsonViewAnnotation = ReflectionUtils.getAnnotation(method, JsonView.class);
            // If one and only one exists, use the @JsonView annotation from the method parameter annotated
            // with @RequestBody. Otherwise fall back to the @JsonView annotation for the method itself.
            jsonViewAnnotationForRequestBody = (JsonView) Arrays.stream(ReflectionUtils.getParameterAnnotations(method))
                    .filter(arr -> Arrays.stream(arr).anyMatch(annotation ->
                            annotation.annotationType()
                                    .equals(io.swagger.v3.oas.annotations.parameters.RequestBody.class)
                    ))
                    .flatMap(Arrays::stream)
                    .filter(annotation -> annotation.annotationType().equals(JsonView.class))
                    .reduce((a, b) -> jsonViewAnnotation)
                    .orElse(jsonViewAnnotation);
        }

        io.swagger.v3.oas.annotations.responses.ApiResponse[] classResponses =
                ReflectionUtils.getRepeatableAnnotationsArray(method.getDeclaringClass(), io.swagger.v3.oas.annotations.responses.ApiResponse.class);
        io.swagger.v3.oas.models.Operation operation = parseMethod(
                method, methodRequestMapping, clazzRequestMappingAnnotation, jsonViewAnnotation, classResponses);

        List<Parameter> operationParameters = new ArrayList<>();
        List<Parameter> formParameters = new ArrayList<>();
        for (java.lang.reflect.Parameter parameter : method.getParameters()) {
            Type parameterizedType = parameter.getParameterizedType();

            if (StringUtils.isBlank(httpMethod)) {
                continue;
            }

            final Type type = TypeFactory.defaultInstance().constructType(parameterizedType, method.getDeclaringClass());

            Set<Annotation> annotations = ANNOTATION_TYPES.stream()
                    .map(at -> AnnotatedElementUtils.findMergedAnnotation(parameter, at))
                    .filter(Objects::nonNull)
                    .map(a -> {
                        Object value = AnnotationUtils.getValue(a, "value");
                        if (value == null || Strings.isNullOrEmpty(value.toString())) {
                            return AnnotationUtils.synthesizeAnnotation(ImmutableMap.of("value", parameter.getName()), a.annotationType(), parameter);
                        } else {
                            return a;
                        }
                    }).collect(Collectors.toSet());

            io.swagger.v3.oas.annotations.Parameter paramAnnotation = AnnotationUtils.getAnnotation(
                    parameter, io.swagger.v3.oas.annotations.Parameter.class);
            if (paramAnnotation != null) {
                annotations.add(paramAnnotation);
            }

            Type paramType = ParameterProcessor.getParameterType(paramAnnotation, true);
            if (paramType == null) {
                paramType = type;
            } else {
                if (!(paramType instanceof Class)) {
                    paramType = type;
                }
            }

            final Components components = globalElementReader.getComponents();
            ResolvedParameter resolvedParameter = extension.extractParameters(
                    ImmutableList.copyOf(annotations), paramType,
                    new HashSet<>(), components, methodRequestMapping, clazzRequestMappingAnnotation,
                    true, jsonViewAnnotation, null);

            operationParameters.addAll(resolvedParameter.parameters);
            // collect params to use together as request Body
            formParameters.addAll(resolvedParameter.formParameters);
            if (resolvedParameter.requestBody != null) {
                processRequestBody(
                        resolvedParameter.requestBody, operation, methodRequestMapping, clazzRequestMappingAnnotation,
                        parameter.getDeclaredAnnotations(), jsonViewAnnotationForRequestBody);
            }
        }

        // if we have form parameters, need to merge them into single schema and use as request body..
        if (formParameters.size() > 0) {
            Schema<?> mergedSchema = new ObjectSchema();
            for (Parameter formParam : formParameters) {
                mergedSchema.addProperties(formParam.getName(), formParam.getSchema());
                if (null != formParam.getRequired() && formParam.getRequired()) {
                    mergedSchema.addRequiredItem(formParam.getName());
                }
            }
            Parameter merged = new Parameter().schema(mergedSchema);
            processRequestBody(
                    merged, operation, methodRequestMapping, clazzRequestMappingAnnotation,
                    new Annotation[0], jsonViewAnnotationForRequestBody);

        }
        if (operationParameters.size() > 0) {
            for (Parameter operationParameter : operationParameters) {
                operation.addParametersItem(operationParameter);
            }
        }

        final Iterator<OpenAPIExtension> chain = OpenAPIExtensions.chain();
        if (chain.hasNext()) {
            final OpenAPIExtension extension = chain.next();
            extension.decorateOperation(operation, method, chain);
        }

        PathItem methodPathItem = new PathItem();
        OpenApiHttpMethod.fromName(httpMethod).pathItemSetter.apply(methodPathItem, operation);
        return Optional.of(Maps.immutableEntry(operationPath, methodPathItem));
    }

    private io.swagger.v3.oas.models.Operation parseMethod(
            Method method,
            RequestMapping methodMapping,
            RequestMapping classMapping,
            JsonView jsonViewAnnotation,
            io.swagger.v3.oas.annotations.responses.ApiResponse[] classResponses) {

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

        io.swagger.v3.oas.models.Operation operation = Optional.ofNullable(apiOperation)
                .map(op -> operationReader.read(op, methodMapping, classMapping, jsonViewAnnotation))
                .orElseGet(io.swagger.v3.oas.models.Operation::new);

        // callbacks
        Map<String, Callback> callbacks = Maps.newLinkedHashMap();

        if (apiCallbacks != null) {
            for (io.swagger.v3.oas.annotations.callbacks.Callback methodCallback : apiCallbacks) {
                Map<String, Callback> currentCallbacks = callbackReader.readCallbacks(methodCallback, methodMapping, classMapping, jsonViewAnnotation);
                callbacks.putAll(currentCallbacks);
            }
        }
        if (callbacks.size() > 0) {
            operation.setCallbacks(callbacks);
        }

        // security
        globalElementReader.getSecurityRequirements().forEach(operation::addSecurityItem);
        if (apiSecurity != null) {
            Optional<List<SecurityRequirement>> requirementsObject = SecurityParser.getSecurityRequirements(apiSecurity.toArray(new io.swagger.v3.oas.annotations.security.SecurityRequirement[apiSecurity.size()]));
            if (requirementsObject.isPresent()) {
                requirementsObject.get().stream()
                        .filter(r -> operation.getSecurity() == null || !operation.getSecurity().contains(r))
                        .forEach(operation::addSecurityItem);
            }
        }

        // servers
        List<io.swagger.v3.oas.models.servers.Server> classServers = globalElementReader.getServers();
        if (classServers != null) {
            classServers.forEach(operation::addServersItem);
        }

        if (apiServers != null) {
            AnnotationsUtils.getServers(apiServers.toArray(new Server[apiServers.size()]))
                    .ifPresent(servers -> servers.forEach(operation::addServersItem));
        }

        // external docs
        AnnotationsUtils.getExternalDocumentation(apiExternalDocumentation).ifPresent(operation::setExternalDocs);

        // method tags
        if (apiTags != null) {
            apiTags.stream()
                    .distinct()
                    .map(io.swagger.v3.oas.annotations.tags.Tag::name)
                    .forEach(operation::addTagsItem);
        }

        // parameters
        if (globalElementReader.getParameters() != null) {
            for (Parameter globalParameter : globalElementReader.getParameters()) {
                operation.addParametersItem(globalParameter);
            }
        }
        final Components components = globalElementReader.getComponents();
        if (apiParameters != null) {
            for (io.swagger.v3.oas.annotations.Parameter parameter : apiParameters) {
                ResolvedParameter resolvedParameter = extension.extractParameters(
                        Collections.singletonList(parameter), ParameterProcessor.getParameterType(parameter),
                        Collections.emptySet(), components, classMapping, methodMapping,
                        true, jsonViewAnnotation, null);

                resolvedParameter.parameters.forEach(operation::addParametersItem);
            }
        }

        // RequestBody in Method
        if (apiRequestBody != null && operation.getRequestBody() == null) {
            OperationParser.getRequestBody(apiRequestBody, classMapping, methodMapping, components, jsonViewAnnotation)
                    .ifPresent(operation::setRequestBody);
        }

        // operation id
        if (StringUtils.isBlank(operation.getOperationId())) {
            operation.setOperationId(operationIdProvider.provideOperationId(method.getName()));
        }

        // classResponses
        if (classResponses != null && classResponses.length > 0) {
            OperationParser.getApiResponses(
                    classResponses,
                    null,
                    null,
                    components,
                    jsonViewAnnotation
            ).ifPresent(responses -> {
                if (operation.getResponses() == null) {
                    operation.setResponses(responses);
                } else {
                    responses.forEach(operation.getResponses()::addApiResponse);
                }
            });
        }

        // apiResponses
        if (apiResponses != null && apiResponses.size() > 0) {
            OperationParser.getApiResponses(
                    apiResponses.toArray(new io.swagger.v3.oas.annotations.responses.ApiResponse[apiResponses.size()]),
                    null,
                    null,
                    components,
                    jsonViewAnnotation
            ).ifPresent(responses -> {
                if (operation.getResponses() == null) {
                    operation.setResponses(responses);
                } else {
                    responses.forEach(operation.getResponses()::addApiResponse);
                }
            });
        }

        // class tags after tags defined as field of @Operation
        Set<Tag> classTags = globalElementReader.getTags();
        if (classTags != null) {
            classTags.stream()
                    .distinct()
                    .map(Tag::getName)
                    .forEach(operation::addTagsItem);
        }

        // handle return type, add as response in case.
        Type returnType = method.getGenericReturnType();
        if (!shouldIgnoreClass(returnType.getTypeName())) {
            ResolvedSchema resolvedSchema = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(returnType).resolveAsRef(true).jsonViewAnnotation(jsonViewAnnotation));
            if (resolvedSchema.schema != null) {
                Schema<?> returnTypeSchema = resolvedSchema.schema;
                Content content = new Content();
                MediaType mediaType = new MediaType().schema(returnTypeSchema);
                AnnotationsUtils.applyTypes(classMapping != null ? classMapping.produces() : new String[0], methodMapping.produces(), content, mediaType);
                ApiResponses apiResponsesModel = Optional.ofNullable(operation.getResponses())
                        .orElseGet(ApiResponses::new);
                operation.responses(apiResponsesModel);
                if (apiResponsesModel.get(DEFAULT_STATUS) == null) {
                    apiResponsesModel.addApiResponse(DEFAULT_STATUS,
                            new ApiResponse().description(SpringOpenApiReader.DEFAULT_DESCRIPTION)
                                    .content(content));
                }
                ApiResponse defaultApiResponse = operation.getResponses().get(DEFAULT_STATUS);
                if (defaultApiResponse != null &&
                        StringUtils.isBlank(defaultApiResponse.get$ref())) {
                    if (defaultApiResponse.getContent() == null) {
                        defaultApiResponse.content(content);
                    } else {
                        for (String key : defaultApiResponse.getContent().keySet()) {
                            if (defaultApiResponse.getContent().get(key).getSchema() == null) {
                                defaultApiResponse.getContent().get(key).setSchema(returnTypeSchema);
                            }
                        }
                    }
                }
                Map<String, Schema> schemaMap = resolvedSchema.referencedSchemas;
                if (schemaMap != null) {
                    schemaMap.forEach(components::addSchemas);
                }

            }
        }

        if (operation.getResponses() == null || operation.getResponses().isEmpty()) {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            AnnotationsUtils.applyTypes(new String[0], new String[0], content, mediaType);

            ApiResponse apiResponseObject = new ApiResponse().description(SpringOpenApiReader.DEFAULT_DESCRIPTION).content(content);
            operation.setResponses(new ApiResponses()._default(apiResponseObject));
        }

        return operation;
    }

    protected void processRequestBody(Parameter requestBodyParameter, io.swagger.v3.oas.models.Operation operation,
                                      RequestMapping methodConsumes, RequestMapping classConsumes,
                                      Annotation[] paramAnnotations,
                                      JsonView jsonViewAnnotation) {

        io.swagger.v3.oas.annotations.parameters.RequestBody requestBodyAnnotation = getRequestBody(Arrays.asList(paramAnnotations));
        if (requestBodyAnnotation != null) {
            Optional<RequestBody> optionalRequestBody = OperationParser.getRequestBody(
                    requestBodyAnnotation, classConsumes, methodConsumes, globalElementReader.getComponents(), jsonViewAnnotation);

            if (optionalRequestBody.isPresent()) {
                RequestBody requestBody = optionalRequestBody.get();
                if (StringUtils.isBlank(requestBody.get$ref()) &&
                        (requestBody.getContent() == null || requestBody.getContent().isEmpty())) {
                    if (requestBodyParameter.getSchema() != null) {
                        Content content = processContent(requestBody.getContent(), requestBodyParameter.getSchema(), methodConsumes, classConsumes);
                        requestBody.setContent(content);
                    }
                } else if (StringUtils.isBlank(requestBody.get$ref()) &&
                        requestBody.getContent() != null &&
                        !requestBody.getContent().isEmpty()) {
                    if (requestBodyParameter.getSchema() != null) {
                        for (MediaType mediaType : requestBody.getContent().values()) {
                            if (mediaType.getSchema() == null) {
                                if (requestBodyParameter.getSchema() == null) {
                                    mediaType.setSchema(new Schema<>());
                                } else {
                                    mediaType.setSchema(requestBodyParameter.getSchema());
                                }
                            }
                            if (StringUtils.isBlank(mediaType.getSchema().getType())) {
                                mediaType.getSchema().setType(requestBodyParameter.getSchema().getType());
                            }
                        }
                    }
                }
                operation.setRequestBody(requestBody);
            }
        } else {
            if (operation.getRequestBody() == null) {
                boolean isRequestBodyEmpty = true;
                RequestBody requestBody = new RequestBody();
                if (StringUtils.isNotBlank(requestBodyParameter.get$ref())) {
                    requestBody.set$ref(requestBodyParameter.get$ref());
                    isRequestBodyEmpty = false;
                }
                if (StringUtils.isNotBlank(requestBodyParameter.getDescription())) {
                    requestBody.setDescription(requestBodyParameter.getDescription());
                    isRequestBodyEmpty = false;
                }
                if (Boolean.TRUE.equals(requestBodyParameter.getRequired())) {
                    requestBody.setRequired(requestBodyParameter.getRequired());
                    isRequestBodyEmpty = false;
                }

                if (requestBodyParameter.getSchema() != null) {
                    Content content = processContent(null, requestBodyParameter.getSchema(), methodConsumes, classConsumes);
                    requestBody.setContent(content);
                    isRequestBodyEmpty = false;
                }
                if (!isRequestBodyEmpty) {
                    operation.setRequestBody(requestBody);
                }
            }
        }
    }

    protected Content processContent(Content content, Schema<?> schema, RequestMapping methodConsumes, RequestMapping classConsumes) {
        Content bodyContent = Optional.ofNullable(content).orElseGet(Content::new);

        String[] mediaTypes = Optional.ofNullable(methodConsumes)
                .map(RequestMapping::consumes)
                .filter(c -> c.length > 0)
                .orElseGet(() -> Optional.ofNullable(classConsumes)
                        .map(RequestMapping::consumes)
                        .filter(c -> c.length > 0)
                        .orElse(new String[]{DEFAULT_MEDIA_TYPE_VALUE}));

        for (String mediaType : mediaTypes) {
            MediaType mediaTypeObject = new MediaType();
            mediaTypeObject.setSchema(schema);
            bodyContent.addMediaType(mediaType, mediaTypeObject);
        }

        return bodyContent;
    }

    private io.swagger.v3.oas.annotations.parameters.RequestBody getRequestBody(List<Annotation> annotations) {
        if (annotations == null) {
            return null;
        }
        for (Annotation a : annotations) {
            if (a instanceof io.swagger.v3.oas.annotations.parameters.RequestBody) {
                return (io.swagger.v3.oas.annotations.parameters.RequestBody) a;
            }
        }
        return null;
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

    private boolean shouldIgnoreClass(String className) {
        if (StringUtils.isBlank(className)) {
            return true;
        }
        boolean ignore = false;
        String rawClassName = className;
        if (rawClassName.startsWith("[")) { // jackson JavaType
            rawClassName = className.replace("[simple type, class ", "");
            rawClassName = rawClassName.substring(0, rawClassName.length() - 1);
        }
        ignore = ignore || rawClassName.startsWith("javax.ws.rs.");
        ignore = ignore || rawClassName.equalsIgnoreCase("void");
        ignore = ignore || ModelConverters.getInstance().isRegisteredAsSkippedClass(rawClassName);
        return ignore;
    }
}
