package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class providing utility methods for Model conversion.
 */
public final class ModelConversionUtils {

    /**
     * Pattern for the extraction of the full name of a class.
     */
    private static final String FULL_CLASS_NAME_STRING_PATTERN = "([a-z]+\\.)+([A-Z][a-zA-Z0-9]+)";
    /**
     * Pattern for the extraction of the full name of a class.
     */
    private static final Pattern FULL_CLASS_NAME_PATTERN = Pattern.compile(FULL_CLASS_NAME_STRING_PATTERN);

    private static final String RESPONSE_ENTITY_CLASS_NAME = "org.springframework.http.ResponseEntity";

    /**
     * Array of all the classes considered as DateTime for the conversion into Property.
     */
    private static final Class<?>[] DATE_CLASSES = {
            Instant.class, LocalDateTime.class, java.util.Date.class, java.sql.Date.class,
    };

    private ModelConversionUtils() {
    }


    /**
     * Verify a type of property corresponds to a Date.
     *
     * @param propertyType the type of property.
     * @return true if the type of the property corresponds to a date (Instant or LocalDateTime)
     */
    public static boolean isDateType(@Nullable Type propertyType) {
        // SimpleType.getTypeName() returns: "[Simple class, java.time.Instant]"
        // -> use a regex in order to extract the real class name.
        String className = getClassName(propertyType);
        return Arrays.stream(DATE_CLASSES).anyMatch(dateClass -> dateClass.getTypeName().equals(className));
    }

    /**
     * Verify if a type corresponds to a ResponseEntity.
     *
     * @param baseType Base type.
     * @return True if the type corresponds to a response entity.
     */
    public static boolean isResponseEntityType(@Nullable Type baseType) {
        String className = getClassName(baseType);
        return RESPONSE_ENTITY_CLASS_NAME.equals(className);
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
                .map(Type::getTypeName)
                .map(propertyTypeName -> {
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
        return ModelEnum.fromClassName(getClassName(inputType));
    }

    /**
     * Extract the first inner type of the given type.
     *
     * @param genericType the input type.
     * @return The type of the first inner element of the generic type (as optional).
     */
    @Nullable
    public static Type extractGenericFirstInnerType(@Nullable Type genericType) {
        final Optional<Type> genericTypeNullable = Optional.ofNullable(genericType);

        return genericTypeNullable
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .map(ParameterizedType::getActualTypeArguments)
                .filter(array -> array.length > 0)
                .map(array -> array[0])
                .orElseGet(() ->
                        genericTypeNullable
                                .filter(TypeBase.class::isInstance)
                                .map(TypeBase.class::cast)
                                .map(TypeBase::getBindings)
                                .filter(negate(TypeBindings::isEmpty))
                                .map(b -> b.getBoundType(0))
                                .orElse(null)
                );
    }

    private static <T> Predicate<T> negate(Predicate<T> input) {
        return input.negate();
    }

}
