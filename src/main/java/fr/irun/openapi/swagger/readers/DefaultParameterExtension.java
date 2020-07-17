package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import fr.irun.openapi.swagger.utils.SpringTypeResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.ParameterProcessor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class DefaultParameterExtension extends AbstractOpenAPIExtension {
    private static final String QUERY_PARAM = "query";
    private static final String HEADER_PARAM = "header";
    private static final String COOKIE_PARAM = "cookie";
    private static final String PATH_PARAM = "path";
    private static final String FORM_PARAM = "form";

    final ObjectMapper mapper = Json.mapper();

    @Override
    @SuppressWarnings("checkstyle:parameternumber")
    public ResolvedParameter extractParameters(List<Annotation> annotations,
                                               Type type,
                                               Set<Type> typesToSkip,
                                               Components components,
                                               RequestMapping classConsumes,
                                               RequestMapping methodConsumes,
                                               boolean includeRequestBody,
                                               JsonView jsonViewAnnotation,
                                               Iterator<OpenAPIExtension> chain) {
        Type relevantType = SpringTypeResolver.resolve(type);
        if (shouldIgnoreType(relevantType, typesToSkip)) {
            return ResolvedParameter.EMPTY;
        }

        Parameter parameter = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof RequestParam) {
                RequestParam param = (RequestParam) annotation;
                Parameter qp = new Parameter();
                qp.setIn(QUERY_PARAM);
                qp.setName(param.value());
                parameter = qp;
            } else if (annotation instanceof PathVariable) {
                PathVariable param = (PathVariable) annotation;
                Parameter pp = new Parameter();
                pp.setIn(PATH_PARAM);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof MatrixVariable) {
                MatrixVariable param = (MatrixVariable) annotation;
                Parameter pp = new Parameter();
                pp.setIn(PATH_PARAM);
                pp.setStyle(Parameter.StyleEnum.MATRIX);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof RequestHeader) {
                RequestHeader param = (RequestHeader) annotation;
                Parameter pp = new Parameter();
                pp.setIn(HEADER_PARAM);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof CookieValue) {
                CookieValue param = (CookieValue) annotation;
                Parameter pp = new Parameter();
                pp.setIn(COOKIE_PARAM);
                pp.setName(param.value());
                parameter = pp;
            } else if (annotation instanceof io.swagger.v3.oas.annotations.Parameter) {
                if (((io.swagger.v3.oas.annotations.Parameter) annotation).hidden()) {
                    return ResolvedParameter.EMPTY;
                }
                if (parameter == null) {
                    parameter = new Parameter();
                }
                if (StringUtils.isNotBlank(((io.swagger.v3.oas.annotations.Parameter) annotation).ref())) {
                    parameter.$ref(((io.swagger.v3.oas.annotations.Parameter) annotation).ref());
                }
            } else {
                List<Parameter> formParameters = new ArrayList<>();
                List<Parameter> parameters = new ArrayList<>();
                if (handleAdditionalAnnotation(
                        parameters, formParameters, annotation, relevantType, typesToSkip, classConsumes, methodConsumes,
                        components, includeRequestBody, jsonViewAnnotation)) {
                    ResolvedParameter extractParametersResult = ResolvedParameter.EMPTY;
                    extractParametersResult.getParameters().addAll(parameters);
                    extractParametersResult.getFormParameters().addAll(formParameters);
                    return extractParametersResult;
                }
            }
        }
        List<Parameter> parameters = new ArrayList<>();
        List<Parameter> processedParameters = new ArrayList<>();
        List<Parameter> processedFormParameters = new ArrayList<>();
        Parameter processedRequestBody = null;

        if (parameter != null && (StringUtils.isNotBlank(parameter.getIn()) || StringUtils.isNotBlank(parameter.get$ref()))) {
            parameters.add(parameter);
        } else if (includeRequestBody) {
            Parameter unknownParameter = ParameterProcessor.applyAnnotations(
                    null,
                    relevantType,
                    annotations,
                    components,
                    classConsumes == null ? new String[0] : classConsumes.value(),
                    methodConsumes == null ? new String[0] : methodConsumes.value(), jsonViewAnnotation);
            if (unknownParameter != null) {
                if (StringUtils.isNotBlank(unknownParameter.getIn()) && !FORM_PARAM.equals(unknownParameter.getIn())) {
                    processedParameters.add(unknownParameter);
                } else if (FORM_PARAM.equals(unknownParameter.getIn())) {
                    unknownParameter.setIn(null);
                    processedFormParameters.add(unknownParameter);
                } else {
                    processedRequestBody = unknownParameter;
                }
            }
        }
        for (Parameter p : parameters) {
            Parameter processedParameter = ParameterProcessor.applyAnnotations(
                    p,
                    relevantType,
                    annotations,
                    components,
                    classConsumes == null ? new String[0] : classConsumes.value(),
                    methodConsumes == null ? new String[0] : methodConsumes.value(),
                    jsonViewAnnotation);
            if (processedParameter != null) {
                processedParameters.add(processedParameter);
            }
        }
        return new ResolvedParameter(processedParameters, processedRequestBody, processedFormParameters);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private boolean handleAdditionalAnnotation(List<Parameter> parameters, List<Parameter> formParameters, Annotation annotation,
                                               final Type type, Set<Type> typesToSkip, RequestMapping classConsumes,
                                               RequestMapping methodConsumes, Components components, boolean includeRequestBody,
                                               JsonView jsonViewAnnotation) {
        boolean processed = false;
        if (RequestBody.class.isAssignableFrom(annotation.getClass())) {
            // Use Jackson's logic for processing Beans
            final BeanDescription beanDesc = mapper.getSerializationConfig().introspect(constructType(type));
            final List<BeanPropertyDefinition> properties = beanDesc.findProperties();

            for (final BeanPropertyDefinition propDef : properties) {
                final AnnotatedField field = propDef.getField();
                final AnnotatedMethod setter = propDef.getSetter();
                final AnnotatedMethod getter = propDef.getGetter();
                final List<Annotation> paramAnnotations = new ArrayList<Annotation>();
                final Iterator<OpenAPIExtension> extensions = OpenAPIExtensions.chain();
                Type paramType = null;

                // Gather the field's details
                if (field != null) {
                    paramType = field.getType();

                    for (final Annotation fieldAnnotation : field.annotations()) {
                        if (!paramAnnotations.contains(fieldAnnotation)) {
                            paramAnnotations.add(fieldAnnotation);
                        }
                    }
                }

                // Gather the setter's details but only the ones we need
                if (setter != null) {
                    // Do not set the param class/type from the setter if the values are already identified
                    if (paramType == null) {
                        // paramType will stay null if there is no parameter
                        paramType = setter.getParameterType(0);
                    }

                    for (final Annotation fieldAnnotation : setter.annotations()) {
                        if (!paramAnnotations.contains(fieldAnnotation)) {
                            paramAnnotations.add(fieldAnnotation);
                        }
                    }
                }

                // Gather the getter's details but only the ones we need
                if (getter != null) {
                    // Do not set the param class/type from the getter if the values are already identified
                    if (paramType == null) {
                        paramType = getter.getType();
                    }

                    for (final Annotation fieldAnnotation : getter.annotations()) {
                        if (!paramAnnotations.contains(fieldAnnotation)) {
                            paramAnnotations.add(fieldAnnotation);
                        }
                    }
                }

                if (paramType == null) {
                    continue;
                }

                // Re-process all Bean fields and let the default swagger-jaxrs/swagger-jersey-jaxrs processors do their thing
                ResolvedParameter resolvedParameter = extensions.next().extractParameters(
                        paramAnnotations,
                        paramType,
                        typesToSkip,
                        components,
                        classConsumes,
                        methodConsumes,
                        includeRequestBody,
                        jsonViewAnnotation,
                        extensions);

                List<Parameter> extractedParameters = resolvedParameter.getParameters();

                for (Parameter p : extractedParameters) {
                    Parameter processedParam = ParameterProcessor.applyAnnotations(
                            p,
                            paramType,
                            paramAnnotations,
                            components,
                            classConsumes == null ? new String[0] : classConsumes.value(),
                            methodConsumes == null ? new String[0] : methodConsumes.value(),
                            jsonViewAnnotation);
                    if (processedParam != null) {
                        parameters.add(processedParam);
                    }
                }

                List<Parameter> extractedFormParameters =
                        resolvedParameter.getFormParameters();

                for (Parameter p : extractedFormParameters) {
                    Parameter processedParam = ParameterProcessor.applyAnnotations(
                            p,
                            paramType,
                            paramAnnotations,
                            components,
                            classConsumes == null ? new String[0] : classConsumes.value(),
                            methodConsumes == null ? new String[0] : methodConsumes.value(),
                            jsonViewAnnotation);
                    if (processedParam != null) {
                        formParameters.add(processedParam);
                    }
                }

                processed = true;
            }
        }
        return processed;
    }

    @Override
    protected boolean shouldIgnoreClass(Class<?> cls) {
        return cls.getName().startsWith("org.springframework.http.server.");
    }

}
