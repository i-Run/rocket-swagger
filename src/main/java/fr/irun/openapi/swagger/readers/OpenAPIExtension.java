package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface OpenAPIExtension {

    String extractOperationMethod(Method method, Iterator<OpenAPIExtension> chain);

    @SuppressWarnings("checkstyle:parameternumber")
    ResolvedParameter extractParameters(
            List<Annotation> annotations, Type type, Set<Type> typesToSkip, Components components,
            RequestMapping classConsumes, RequestMapping methodConsumes, boolean includeRequestBody,
            JsonView jsonViewAnnotation, Iterator<OpenAPIExtension> chain);

    /**
     * Decorates operation with additional vendor based extensions.
     *
     * @param operation the operation, build from swagger definition
     * @param method    the method for additional scan
     * @param chain     the chain with swagger extensions to process
     */
    void decorateOperation(Operation operation, Method method, Iterator<OpenAPIExtension> chain);
}
