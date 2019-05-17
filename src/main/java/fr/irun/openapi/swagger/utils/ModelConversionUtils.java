package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class providing utility methods for Model conversion.
 */
public final class ModelConversionUtils {


    /**
     * Name of the Flux type.
     */
    private static final String REACTOR_FLUX_CLASS_NAME = Flux.class.getName();

    /**
     * Name of the Mono type.
     */
    private static final String REACTOR_MONO_CLASS_NAME = Mono.class.getName();

    /**
     * Name of the class "Entity" of Hexamon - specific actions to perform for this class.
     */
    private static final String HEXAMON_ENTITY_CLASS_NAME = "fr.irun.hexamon.api.entity.Entity";

    /**
     * Name of the Nested class for the CMS.
     */
    private static final String CMS_NESTED_CLASS_NAME = "fr.irun.cms.api.model.Nested";

    /**
     * Model enum ti use, mapped by class names.
     */
    private static final Map<String, ModelEnum> MODEL_TYPES_BY_CLASS_NAMES = ImmutableMap.of(
            HEXAMON_ENTITY_CLASS_NAME, ModelEnum.ENTITY,
            REACTOR_FLUX_CLASS_NAME, ModelEnum.FLUX,
            REACTOR_MONO_CLASS_NAME, ModelEnum.MONO,
            CMS_NESTED_CLASS_NAME, ModelEnum.NESTED
    );

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
        String className = getFullClassName(propertyType);
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
        String className = getFullClassName(propertyType);
        return Arrays.stream(NOT_RESOLVABLE_CLASSES).anyMatch(dateClass -> dateClass.getTypeName().equals(className));
    }


    /**
     * Obtain the name of the class related to a given type.
     * A generic class will not include its internal types.
     *
     * @param type type to extract the class name.
     * @return The full name of the related class.
     */
    private static String getFullClassName(@Nullable Type type) {

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
     * Compute the type of a model class.
     *
     * @param inputType the type of the input model.
     * @return the type of model to consider.
     */
    public static ModelEnum computeModelType(Type inputType) {
        return MODEL_TYPES_BY_CLASS_NAMES.entrySet().stream()
                .filter(entry -> doesTypeMatchAnyClass(inputType, entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(ModelEnum.STANDARD);
    }

    /**
     * Verify the given type matches at least one of the given class.
     *
     * @param type             type to check.
     * @param classesFullNames the  full names of the classes to verify.
     * @return true if the type matches at least one of the given classes names.
     */
    private static boolean doesTypeMatchAnyClass(Type type, String... classesFullNames) {
        String typeName = getFullClassName(type);
        return Arrays.asList(classesFullNames).contains(typeName);
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
     * Copy a model to another ModelImpl instance.
     *
     * @param newModelName      Name of the new model.
     * @param newModelReference Reference of the new model.
     * @param inputModel        Model to copy.
     * @return the copied Model.
     */
    public static ModelImpl copyModel(String newModelName, String newModelReference, @Nullable Model inputModel) {
        return Optional.ofNullable(inputModel)
                .map(m -> {
                    final ModelImpl model = new ModelImpl();
                    model.setName(newModelName);
                    model.setDescription(m.getDescription());
                    model.setReference(newModelReference);
                    model.setTitle(m.getTitle());
                    model.setExternalDocs(m.getExternalDocs());
                    model.setExample(m.getExample());
                    return model;
                }).orElseGet(ModelImpl::new);
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
        List<Type> outputTypes = new ArrayList<>();
        Type nextInnerType = inputType;
        while (nextInnerType != null) {
            outputTypes.add(0, nextInnerType);
            nextInnerType = extractGenericFirstInnerType(nextInnerType);
        }
        return outputTypes;
    }

    /**
     * Split the given string with given separator and extract the last result of the split.
     *
     * @param input     String to split (can be null).
     * @param separator separator.
     * @return the last value of the split, empty String if the input is null or empty.
     */
    public static String extractLastSplitResult(@Nullable String input, String separator) {
        return Optional.ofNullable(input)
                .map(s -> {
                    String[] split = s.split(separator);
                    if (split.length > 0) {
                        return split[split.length - 1];
                    }
                    return "";
                }).orElse("");
    }

}
