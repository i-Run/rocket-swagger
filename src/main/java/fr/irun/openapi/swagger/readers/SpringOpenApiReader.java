package fr.irun.openapi.swagger.readers;

import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.utils.OpenAPIComponentsHelper;
import fr.irun.openapi.swagger.utils.OpenApiHttpMethod;
import fr.irun.openapi.swagger.utils.OperationIdProvider;
import fr.irun.openapi.swagger.utils.ReaderUtils;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.integration.ContextUtils;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SpringOpenApiReader implements OpenApiReader {
    public static final String DEFAULT_MEDIA_TYPE_VALUE = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
    public static final String DEFAULT_DESCRIPTION = "successful operation";
    public static final String DEFAULT_RESPONSE_STATUS = Integer.toString(HttpStatus.OK.value());

    private OpenAPIConfiguration config;
    private OpenAPI openAPI;

    public SpringOpenApiReader() {
        this.openAPI = new OpenAPI();
    }

    public SpringOpenApiReader(OpenAPI openAPI) {
        this();
        setConfiguration(new SwaggerConfiguration().openAPI(openAPI));
    }

    @SuppressWarnings("unused")
    public SpringOpenApiReader(OpenAPIConfiguration openApiConfiguration) {
        this();
        setConfiguration(openApiConfiguration);
    }

    @SuppressWarnings("unused")
    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    /**
     * Scans a single class for Swagger annotations - does not invoke ReaderListeners
     *
     * @param cls The {@code Class} object to analyze
     * @return the {@link OpenAPI} result object
     */
    public OpenAPI read(Class<?> cls) {
        log.debug("read single class");
        return read(cls, resolveApplicationPath());
    }

    /**
     * Scans a set of classes for both ReaderListeners and OpenAPI annotations. All found listeners will
     * be instantiated before any of the classes are scanned for OpenAPI annotations - so they can be invoked
     * accordingly.
     *
     * @param classes a set of classes to scan
     * @return the generated OpenAPI definition
     */
    public OpenAPI read(Set<Class<?>> classes) {
        log.debug("read multiple classes");
        log.debug("classes: {}", classes);
        Set<Class<?>> sortedClasses = new TreeSet<>((class1, class2) -> {
            if (class1.equals(class2)) {
                return 0;
            } else if (class1.isAssignableFrom(class2)) {
                return -1;
            } else if (class2.isAssignableFrom(class1)) {
                return 1;
            }
            return class1.getName().compareTo(class2.getName());
        });
        sortedClasses.addAll(classes);

        Map<Class<?>, ReaderListener> listeners = new HashMap<>();

        for (Class<?> cls : sortedClasses) {
            if (ReaderListener.class.isAssignableFrom(cls) && !listeners.containsKey(cls)) {
                try {
                    listeners.put(cls, (ReaderListener) cls.newInstance());
                } catch (Exception e) {
                    log.error("Failed to create ReaderListener", e);
                }
            }
        }

        for (ReaderListener listener : listeners.values()) {
            try {
                listener.beforeScan(this, openAPI);
            } catch (Exception e) {
                log.error("Unexpected error invoking beforeScan listener [" + listener.getClass().getName() + "]", e);
            }
        }

        for (Class<?> cls : sortedClasses) {
            read(cls, resolveApplicationPath());
        }

        for (ReaderListener listener : listeners.values()) {
            try {
                listener.afterScan(this, openAPI);
            } catch (Exception e) {
                log.error("Unexpected error invoking afterScan listener [" + listener.getClass().getName() + "]", e);
            }
        }
        return openAPI;
    }

    @Override
    public void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        if (openApiConfiguration != null) {
            this.config = ContextUtils.deepCopy(openApiConfiguration);
            if (openApiConfiguration.getOpenAPI() != null) {
                this.openAPI = this.config.getOpenAPI();
            }
        } else {
            this.config = new SwaggerConfiguration();
        }
    }

    public OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        log.debug("read multiple classes with resources...");
        return read(classes);
    }

    protected String resolveApplicationPath() {
        log.debug("resolveApplicationPath ...");
        return "";
    }

    public OpenAPI read(Class<?> cls, String parentPath) {

        log.debug("read class {}, parentPath: {}...", cls, parentPath);

        Hidden hidden = cls.getAnnotation(Hidden.class);

        boolean isRestController = AnnotatedElementUtils.isAnnotated(cls, RestController.class);
        String classMediaType = (isRestController) ? MediaType.APPLICATION_JSON_VALUE : MediaType.ALL_VALUE;
        RequestMapping requestMappingAnnotation = AnnotatedElementUtils.findMergedAnnotation(cls, RequestMapping.class);
        if (isRestController && (requestMappingAnnotation == null || requestMappingAnnotation.produces().length == 0)) {
            Map<String, Object> annotationAttributes = Optional.ofNullable(requestMappingAnnotation)
                    .map(AnnotationUtils::getAnnotationAttributes)
                    .orElseGet(HashMap::new);
            annotationAttributes.put("produces", classMediaType);
            requestMappingAnnotation = AnnotationUtils.synthesizeAnnotation(
                    annotationAttributes, RequestMapping.class, cls);
        }
        final RequestMapping apiRequestMapping = requestMappingAnnotation;

        if (hidden != null) {
            return openAPI;
        }

        readOpenAPIDefinition(cls);

        final GlobalElementReader globalElementReader = new GlobalElementReader(openAPI);

        OpenAPIComponentsReader.readSecuritySchemes(cls)
                .forEach(globalElementReader.getComponents()::addSecuritySchemes);

        // class tags, consider only name to add to class operations
        io.swagger.v3.oas.annotations.tags.Tag[] apiTags =
                ReflectionUtils.getRepeatableAnnotationsArray(cls, io.swagger.v3.oas.annotations.tags.Tag.class);
        if (apiTags != null) {
            AnnotationsUtils.getTags(apiTags, false)
                    .ifPresent(tags -> tags.forEach(globalElementReader.getTags()::add));
        }

        // servers
        io.swagger.v3.oas.annotations.servers.Server[] apiServers =
                ReflectionUtils.getRepeatableAnnotationsArray(cls, io.swagger.v3.oas.annotations.servers.Server.class);
        if (apiServers != null) {
            AnnotationsUtils.getServers(apiServers).ifPresent(globalElementReader.getServers()::addAll);
        }

        // look for constructor-level annotated properties
        globalElementReader.getParameters().addAll(
                ReaderUtils.collectConstructorParameters(cls, globalElementReader.getComponents(), apiRequestMapping, null));

        // look for field-level annotated properties
        globalElementReader.getParameters().addAll(
                ReaderUtils.collectFieldParameters(cls, globalElementReader.getComponents(), apiRequestMapping, null));

        OperationIdProvider operationIdProvider = new OperationIdProvider().load(openAPI);
        OperationReader operationReader = new OperationReader(operationIdProvider, globalElementReader, Iterators.getLast(OpenAPIExtensions.chain()));
        CallbackReader callbackReader = new CallbackReader(operationReader);
        ClazzMethodReader clazzMethodReader = new ClazzMethodReader(
                config.isReadAllResources(),
                config.getIgnoredRoutes() != null ? config.getIgnoredRoutes() : Collections.emptyList(),
                globalElementReader,
                callbackReader, operationReader, operationIdProvider,
                Iterators.getLast(OpenAPIExtensions.chain()), apiRequestMapping, parentPath
        );
        // iterate class methods
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            Optional<Map.Entry<String, PathItem>> oPathItem = clazzMethodReader.read(method);
            oPathItem.ifPresent(path -> {
                PathItem originalPathItem;
                if (openAPI.getPaths() != null && openAPI.getPaths().get(path.getKey()) != null) {
                    originalPathItem = openAPI.getPaths().get(path.getKey());
                    for (OpenApiHttpMethod value : OpenApiHttpMethod.values()) {
                        Operation operation = value.pathItemGetter.apply(path.getValue());
                        value.pathItemSetter.apply(originalPathItem, operation);
                    }
                } else {
                    originalPathItem = path.getValue();
                }
                openAPI.path(path.getKey(), originalPathItem);
            });

        }

        final Components components = globalElementReader.getComponents();
        // if no components object is defined in openApi instance passed by client, set openAPI.components to resolved components (if not empty)
        if (!OpenAPIComponentsHelper.isNullOrEmpty((components))) {
            Components mergedComponents = OpenAPIComponentsHelper.mergeAllComponents(openAPI.getComponents(), components);
            if (!OpenAPIComponentsHelper.isNullOrEmpty(mergedComponents)) {
                openAPI.setComponents(mergedComponents);
            }
        }

        // add tags from class to definition tags
        List<Tag> globalTagsSection = Stream.concat(globalElementReader.getTags().stream(),
                Optional.ofNullable(openAPI.getTags()).map(List::stream).orElse(Stream.empty()))
                .distinct()
                .sorted(Comparator.comparing(Tag::getName))
                .collect(Collectors.toList());
        if (!globalTagsSection.isEmpty()) {
            openAPI.tags(globalTagsSection);
        }

        return openAPI;
    }

    private void readOpenAPIDefinition(Class<?> cls) {
        OpenAPIDefinition openAPIDefinition = ReflectionUtils.getAnnotation(cls, OpenAPIDefinition.class);

        if (openAPIDefinition == null) {
            return;
        }

        AnnotationsUtils.getInfo(openAPIDefinition.info()).ifPresent(openAPI::setInfo);

        // OpenApiDefinition security requirements
        SecurityParser.getSecurityRequirements(openAPIDefinition.security())
                .ifPresent(openAPI::setSecurity);
        //
        // OpenApiDefinition external docs
        AnnotationsUtils
                .getExternalDocumentation(openAPIDefinition.externalDocs())
                .ifPresent(openAPI::setExternalDocs);

        // OpenApiDefinition tags
        AnnotationsUtils.getTags(openAPIDefinition.tags(), false)
                .ifPresent(tags -> openAPI.getTags().addAll(tags));

        // OpenApiDefinition servers
        AnnotationsUtils.getServers(openAPIDefinition.servers()).ifPresent(openAPI::setServers);

        // OpenApiDefinition extensions
        if (openAPIDefinition.extensions().length > 0) {
            openAPI.setExtensions(AnnotationsUtils
                    .getExtensions(openAPIDefinition.extensions()));
        }
    }

}
