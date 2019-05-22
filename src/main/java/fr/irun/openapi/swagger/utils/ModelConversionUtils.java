package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class providing utility methods for Model conversion.
 */
public final class ModelConversionUtils {

    /**
     * Prefix for the model reference.
     */
    private static final String REFERENCE_PREFIX = "#/definitions/";

    /**
     * Pattern for the extraction of the full name of a class.
     */
    private static final String FULL_CLASS_NAME_STRING_PATTERN = "([a-z]+\\.)+([A-Z][a-zA-Z0-9]+)";
    /**
     * Pattern for the extraction of the full name of a class.
     */
    private static final Pattern FULL_CLASS_NAME_PATTERN = Pattern.compile(FULL_CLASS_NAME_STRING_PATTERN);

    /**
     * Array of all the classes considered as DateTime for the conversion into Property.
     */
    private static final Class<?>[] DATE_CLASSES = {
            Instant.class, LocalDateTime.class, java.util.Date.class, java.sql.Date.class,
    };

    /**
     * Array of the classes which cannot be resolved.
     */
    private static final Class<?>[] NOT_RESOLVABLE_CLASSES = {
            JsonNode.class,
    };


    private ModelConversionUtils() {

    }


    /**
     * Verify a type of property corresponds to a Date.
     *
     * @param propertyType the type of property.
     * @return true if the type of the property corresponds to a date (Instant or LocalDateTime)
     */
    public static boolean isDateType(Type propertyType) {
        // SimpleType.getTypeName() returns: "[Simple class, java.time.Instant]"
        // -> use a regex in order to extract the real class name.
        String className = getClassName(propertyType);
        return Arrays.stream(DATE_CLASSES).anyMatch(dateClass -> dateClass.getTypeName().equals(className));
    }

    /**
     * Verify a type of property corresponds to a type not resolvable.
     *
     * @param propertyType input type.
     * @return true if the input type cannot be resolved.
     */
    public static boolean isUnresolvableType(Type propertyType) {
        // SimpleType.getTypeName() returns: "[Simple class, java.time.JsonNode]"
        // -> use a regex in order to extract the real class name.
        String className = getClassName(propertyType);
        return Arrays.stream(NOT_RESOLVABLE_CLASSES).anyMatch(dateClass -> dateClass.getTypeName().equals(className));
    }


    /**
     * Obtain the name of the class related to a given type (with package).
     * A generic class will not include its internal types.
     *
     * @param type type to extract the class name.
     * @return The full name of the related class.
     */
    static String getClassName(@Nullable Type type) {

        return Optional.ofNullable(type)
                .map(t -> {
                    final String propertyTypeName = t.getTypeName();
                    final Matcher matcher = FULL_CLASS_NAME_PATTERN.matcher(propertyTypeName);
                    if (matcher.find()) {
                        return matcher.group();
                    }
                    return "";
                }).orElse("");
    }

    /**
     * Obtain the simple class name for a type.
     *
     * @param type Input type.
     * @return The simple class name.
     */
    public static String getSimpleClassName(@Nullable Type type) {
        final String[] classWords = getClassName(type).split("\\.");
        return Arrays.stream(classWords)
                .skip(classWords.length - 1L)
                .findFirst()
                .orElse("");
    }


    /**
     * Compute the type of a model class.
     *
     * @param inputType the type of the input model.
     * @return the type of model to consider.
     */
    public static ModelEnum computeModelType(Type inputType) {
        return ModelEnum.fromClassName(getClassName(inputType));
    }

    /**
     * Extract the first inner type of the given type.
     *
     * @param genericType the input type.
     * @return The type of the first inner element of the generic type. Returns null if the input type is not generic.
     */
    public static Type extractGenericFirstInnerType(Type genericType) {
        Type result = null;
        if (genericType instanceof ParameterizedType) {
            final Type[] innerTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            if (innerTypes.length > 0) {
                result = innerTypes[0];
            }
        } else if (genericType instanceof TypeBase) {
            TypeBindings bindings = ((TypeBase) genericType).getBindings();
            if (!bindings.isEmpty()) {
                result = bindings.getBoundType(0);
            }
        }
        return result;
    }

    /**
     * Copy a model to another ModelImpl instance. Do not copy the properties.
     *
     * @param newModelName Name of the new model.
     * @param inputModel   Model to copy.
     * @return the copied Model.
     */
    public static ModelImpl copyModelWithoutProperties(String newModelName, @Nullable Model inputModel) {
        return Optional.ofNullable(inputModel)
                .map(m -> {
                    final ModelImpl model = new ModelImpl();
                    model.setName(newModelName);
                    model.setDescription(m.getDescription());
                    model.setReference(REFERENCE_PREFIX + newModelName);
                    model.setTitle(m.getTitle());
                    model.setExternalDocs(m.getExternalDocs());
                    model.setExample(m.getExample());
                    return model;
                }).orElseGet(ModelImpl::new);
    }

    /**
     * Etxract the inner type of
     *
     * @param inputType type to extract the generic types.
     * @return List of the inner types of the given type. Include the main type at the beginning of the list.
     * * For instance:
     * * <ul>
     * * <li>Mono&lt;Entity&lt;String&gt;&gt;&gt; will return { Mono, Entity, String }.</li>
     * * <li>String will return { String }</li>
     * * </ul>
     */
    static List<Type> extractInnerTypes(Type inputType) {
        List<Type> outputTypes = new ArrayList<>();

        Type nextInnerType = inputType;
        while (nextInnerType != null) {
            outputTypes.add(nextInnerType);
            nextInnerType = extractGenericFirstInnerType(nextInnerType);
        }
        return outputTypes;
    }

    /**
     * Extract all the inner types of the given type and reverse them.
     *
     * @param inputType the input type.
     * @return List of the inner types of the given type, reversed. Include the main type at the end of the list.
     * For instance:
     * <ul>
     * <li>Mono&lt;Entity&lt;String&gt;&gt;&gt; will return { String, Entity, Mono }.</li>
     * <li>String will return { String }</li>
     * </ul>
     */
    public static List<Type> extractInnerTypesReversed(Type inputType) {
        final List<Type> list = extractInnerTypes(inputType);
        Collections.reverse(list);
        return list;
    }

}
