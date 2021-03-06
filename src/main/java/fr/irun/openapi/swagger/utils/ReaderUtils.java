package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Sets;
import fr.irun.openapi.swagger.readers.OpenAPIExtension;
import fr.irun.openapi.swagger.readers.OpenAPIExtensions;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ReaderUtils {
    private static final String PATH_DELIMITER = "/";

    private ReaderUtils() {
    }

    /**
     * Collects constructor-level parameters from class.
     *
     * @param cls                is a class for collecting
     * @param components         OpenAPI Components
     * @param classConsumes      {@link RequestMapping} annotation from the read class
     * @param jsonViewAnnotation {@link JsonView} annotation from the read class
     * @return the collection of supported parameters
     */
    public static List<Parameter> collectConstructorParameters(
            Class<?> cls, Components components, RequestMapping classConsumes, JsonView jsonViewAnnotation) {

        if (cls.isLocalClass() || (cls.isMemberClass() && !Modifier.isStatic(cls.getModifiers()))) {
            return Collections.emptyList();
        }

        List<Parameter> selected = Collections.emptyList();
        int maxParamsCount = 0;

        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            if (!ReflectionUtils.isConstructorCompatible(constructor)
                    && !ReflectionUtils.isInject(Arrays.asList(constructor.getDeclaredAnnotations()))) {
                continue;
            }

            final Type[] genericParameterTypes = constructor.getGenericParameterTypes();
            final Annotation[][] annotations = constructor.getParameterAnnotations();

            int paramsCount = 0;
            final List<Parameter> parameters = new ArrayList<>();
            for (int i = 0; i < genericParameterTypes.length; i++) {
                final List<Annotation> tmpAnnotations = Arrays.asList(annotations[i]);
                if (isContext(tmpAnnotations)) {
                    paramsCount++;
                } else {
                    final Type genericParameterType = genericParameterTypes[i];
                    final List<Parameter> tmpParameters = collectParameters(
                            genericParameterType, tmpAnnotations, components, classConsumes, jsonViewAnnotation);
                    if (tmpParameters.size() >= 1) {
                        for (Parameter tmpParameter : tmpParameters) {
                            Parameter processedParameter = ParameterProcessor.applyAnnotations(
                                    tmpParameter,
                                    genericParameterType,
                                    tmpAnnotations,
                                    components,
                                    classConsumes == null ? new String[0] : classConsumes.value(),
                                    null,
                                    jsonViewAnnotation);
                            if (processedParameter != null) {
                                parameters.add(processedParameter);
                            }
                        }
                        paramsCount++;
                    }
                }
            }

            if (paramsCount >= maxParamsCount) {
                maxParamsCount = paramsCount;
                selected = parameters;
            }
        }

        return selected;
    }

    /**
     * Collects field-level parameters from class.
     *
     * @param cls                is a class for collecting
     * @param components         OpenAPI Components
     * @param classConsumes      {@link RequestMapping} annotation from the read class
     * @param jsonViewAnnotation {@link JsonView} annotation from the read class
     * @return the collection of supported parameters
     */
    public static List<Parameter> collectFieldParameters(
            Class<?> cls, Components components, RequestMapping classConsumes, JsonView jsonViewAnnotation) {
        final List<Parameter> parameters = new ArrayList<>();
        for (Field field : ReflectionUtils.getDeclaredFields(cls)) {
            final List<Annotation> annotations = Arrays.asList(field.getAnnotations());
            final Type genericType = field.getGenericType();
            parameters.addAll(collectParameters(genericType, annotations, components, classConsumes, jsonViewAnnotation));
        }
        return parameters;
    }

    private static List<Parameter> collectParameters(
            Type type, List<Annotation> annotations, Components components, RequestMapping classConsumes, JsonView jsonViewAnnotation) {
        final Iterator<OpenAPIExtension> chain = OpenAPIExtensions.chain();
        return chain.hasNext()
                ? chain.next().extractParameters(
                annotations, type, Sets.newHashSet(), components,
                classConsumes, null, false,
                jsonViewAnnotation, chain).getParameters()
                : Collections.emptyList();
    }

    private static boolean isContext(List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof ControllerAdvice) {
                return true;
            }
        }
        return false;
    }

    public static Optional<List<String>> getStringListFromStringArray(String[] array) {
        if (array == null) {
            return Optional.empty();
        }
        List<String> list = new ArrayList<>();
        boolean isEmpty = true;
        for (String value : array) {
            if (StringUtils.isNotBlank(value)) {
                isEmpty = false;
            }
            list.add(value);
        }
        if (isEmpty) {
            return Optional.empty();
        }
        return Optional.of(list);
    }

    /**
     * <p>Check if path is contained inside a list of path. As plain path or as sub-path</p>
     * <p>For the following ignored list :</p>
     * <ul>
     *     <li>/my/first/route</li>
     *     <li>/my/second</li>
     * </ul>
     * <p>
     * The expected result was:
     * <ul>
     *     <li>{@code /my > false}</li>
     *     <li>{@code /my/first/test > false}</li>
     *     <li>{@code /my/first/route > true}</li>
     *     <li>{@code /my/second/route > true}</li>
     *     <li>{@code /my/second/test > true}</li>
     * </ul>
     *
     * @param path          The path to check
     * @param ignoredRoutes The list of path to ignore
     * @return true if the path is present il the ignoredRoutes as plain path or sub-path
     */
    public static boolean isIgnored(String path, @Nonnull Collection<String> ignoredRoutes) {
        Objects.requireNonNull(ignoredRoutes, "ignoredRoutes is mandatory !");
        for (String item : ignoredRoutes) {
            final int length = item.length();
            if (path.startsWith(item) && (path.length() == length || path.startsWith(PATH_DELIMITER, length))) {
                return true;
            }
        }
        return false;
    }

    public static String getPath(
            RequestMapping classLevelPath, RequestMapping methodLevelPath, String parentPath, boolean isSubresource) {
        if (classLevelPath == null && methodLevelPath == null && StringUtils.isEmpty(parentPath)) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        appendPathComponent(parentPath, b);
        if (classLevelPath != null) {
            if (classLevelPath.path().length > 0 && !isSubresource) {
                appendPathComponent(classLevelPath.path()[0], b);
            }
        }
        if (methodLevelPath != null) {
            if (methodLevelPath.path().length > 0) {
                appendPathComponent(methodLevelPath.path()[0], b);
            }
        }
        return b.length() == 0 ? PATH_DELIMITER : b.toString();
    }

    /**
     * appends a path component string to a StringBuilder
     * guarantees:
     * <ul>
     *     <li>nulls, empty strings and "/" are nops</li>
     *     <li>output will always start with "/" and never end with "/"</li>
     * </ul>
     *
     * @param component component to be added
     * @param to        output
     */
    private static void appendPathComponent(String component, StringBuilder to) {
        if (component == null || component.isEmpty() || PATH_DELIMITER.equals(component)) {
            return;
        }
        if (!component.startsWith(PATH_DELIMITER) && (to.length() == 0 || PATH_DELIMITER.charAt(0) != to.charAt(to.length() - 1))) {
            to.append(PATH_DELIMITER);
        }
        if (component.endsWith(PATH_DELIMITER)) {
            to.append(component, 0, component.length() - 1);
        } else {
            to.append(component);
        }
    }

    public static String extractOperationMethod(Method method, Iterator<OpenAPIExtension> chain) {
        RequestMethod[] methods = Optional.ofNullable(AnnotationUtils.findAnnotation(method, RequestMapping.class))
                .map(RequestMapping::method).orElse(new RequestMethod[]{});
        if (methods.length != 0) {
            return methods[0].name().toLowerCase();
        } else if ((ReflectionUtils.getOverriddenMethod(method)) != null) {
            return extractOperationMethod(ReflectionUtils.getOverriddenMethod(method), chain);
        } else if (chain != null && chain.hasNext()) {
            return chain.next().extractOperationMethod(method, chain);
        } else {
            return null;
        }
    }
}
