package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.utils.PropertyModelConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Customized Model converter used to :
 * <ul>
 * <li>Correctly parse Instants and DateTimes tp JSON</li>
 * <li>Manage the Mono and the Flux of Spring reactor.</li>
 * </ul>
 */
public class ReactorModelConverter implements ModelConverter {

    /**
     * Pattern for the extraction of the full name of a class.
     */
    private static final String FULL_CLASS_NAME_STRING_PATTERN = "([a-z]+\\.)+([A-Z][a-zA-Z]+)";
    /**
     * Pattern for the extraction of the full name of a class.
     */
    private static final Pattern FULL_CLASS_NAME_PATTERN = Pattern.compile(FULL_CLASS_NAME_STRING_PATTERN);
    /**
     * Default empty annotation array used as parameter.
     */
    private static final Annotation[] EMPTY_ANNOTATIONS_ARRAY = new Annotation[0];

    /**
     * Array of all the classes considered as DateTime for the conversion into Property.
     */
    private static final Class<?>[] DATE_CLASSES = {
            Instant.class, LocalDateTime.class,
    };

    /**
     * Default ModelConverter used if this one does not manage a type.
     */
    private final ModelConverter baseConverter = new ModelResolver(new ObjectMapper());

    /**
     * Default instance used to convert a swagger property to a swagger model.
     */
    private final PropertyModelConverter propertyModelConverter = new PropertyModelConverter();


    @Override
    public Property resolveProperty(Type type, ModelConverterContext context,
                                    Annotation[] annotations, Iterator<ModelConverter> chain) {
        Property property = null;
        if (type != null) {
            // Case when the property is a date
            if (isDateType(type)) {
                property = new DateTimeProperty();
            } else if (areSameGenericTypes(type, Flux.class)) {
                // case when the property is a Flux<T> ==> convert to a T[]
                Property innerProperty = getInnerTypeOfGenericProperty(type, context, annotations, chain);
                property = new ArrayProperty().items(innerProperty);
            } else if (areSameGenericTypes(type, Mono.class)) {
                // Case when the property is a Mono<T> ==> convert to a T
                property = getInnerTypeOfGenericProperty(type, context, annotations, chain);
            }
            // Otherwise, call the default ModelResolver.
            if (property == null && chain.hasNext()) {
                property = chain.next().resolveProperty(type, context, annotations, chain);
            }
        }
        return property;
    }

    /**
     * Verify a type of property corresponds to a Date.
     *
     * @param propertyType the type of property.
     * @return true if the type of the property corresponds to a date (Instant or LocalDateTime)
     */
    private boolean isDateType(Type propertyType) {
        boolean isDate = false;
        String propertyTypeName = propertyType.getTypeName();
        Matcher matcher = FULL_CLASS_NAME_PATTERN.matcher(propertyTypeName);
        // Extract the full class name with Regex because the name of the types are :
        //   - For the class Instant: "[simple type, class java.time.Instant]"
        //   - Fir the class LocalDateTime: "[simple type, class java.time.LocalDateTime]"
        if (matcher.find()) {
            String realTypeName = matcher.group();
            isDate = Arrays.stream(DATE_CLASSES).anyMatch(dateClass -> dateClass.getTypeName().equals(realTypeName));
        }
        return isDate;
    }

    /**
     * Check a generic type matches a class. Do not check the type of the inner class(es).
     *
     * @param propertyGenericType type of the input generic property.
     * @param targetClass         Target class to check.
     * @return true if the type matches the class.
     */
    private boolean areSameGenericTypes(Type propertyGenericType, Class<?> targetClass) {
        String propertyTypeName = propertyGenericType.getTypeName();
        // Split under '<' to match the generic types Flux and Mono:
        // We do not care about the type of object managed by the Flux/Mono in this type.
        String classTypeName = targetClass.getTypeName().split("<")[0];
        return propertyTypeName.startsWith(classTypeName);
    }

    /**
     * Obtain the Parameter corresponding to an inner type of a generic class.
     *
     * @param type        The type of the generic class.
     * @param context     Context of the converter
     * @param annotations Array of the annotations.
     * @param chain       Chain of converter to use.
     * @return The property relative to the type T if the input type corresponds to a class SomeGenericClass&lt;T.&gt;
     */
    private Property getInnerTypeOfGenericProperty(Type type, ModelConverterContext context,
                                                   Annotation[] annotations, Iterator<ModelConverter> chain) {
        ParameterizedType reactorType = (ParameterizedType) type;
        Type innerType = reactorType.getActualTypeArguments()[0];
        return baseConverter.resolveProperty(innerType, context, annotations, chain);
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        Property property = resolveProperty(type, modelConverterContext, EMPTY_ANNOTATIONS_ARRAY, iterator);
        Model model;

        if (property == null) {
            model = baseConverter.resolve(type, modelConverterContext, iterator);
        } else {
            model = propertyModelConverter.propertyToModel(property);
        }
        return model;
    }

}
