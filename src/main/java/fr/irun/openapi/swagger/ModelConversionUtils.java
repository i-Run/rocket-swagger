package fr.irun.openapi.swagger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class providing utility methods for Model conversion.
 */
final class ModelConversionUtils {

    /**
     * Name of the class "Entity" of Hexamon - specific actions to perform for this class.
     */
    private static final String HEXAMON_ENTITY_CLASS_NAME = "fr.irun.hexamon.api.entity.Entity";

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
            Instant.class, LocalDateTime.class,
    };


    private ModelConversionUtils() {

    }


    /**
     * Verify a type of property corresponds to a Date.
     *
     * @param propertyType the type of property.
     * @return true if the type of the property corresponds to a date (Instant or LocalDateTime)
     */
    static boolean isDateType(Type propertyType) {
        String className = getFullClassName(propertyType);
        return Arrays.stream(DATE_CLASSES).anyMatch(dateClass -> dateClass.getTypeName().equals(className));
    }


    /**
     * Obtain the name of the class related to a given type.
     * A generic class will not include its internal types.
     *
     * @param type type to extract the class name.
     * @return The full name of the related class.
     */
    static String getFullClassName(Type type) {
        String className = "";
        if (type != null) {
            String propertyTypeName = type.getTypeName();
            Matcher matcher = FULL_CLASS_NAME_PATTERN.matcher(propertyTypeName);
            if (matcher.find()) {
                className = matcher.group();
            }
        }
        return className;
    }


    /**
     * Verify a type is an Hexamon entity.
     *
     * @param propertyType Type of the property
     * @return true if the element is an hexamon entity (specific actions to perform).
     */
    static boolean isHexamonEntityType(Type propertyType) {
        boolean isEntity = false;
        if (propertyType != null) {
            String propertyTypeName = propertyType.getTypeName();
            Matcher matcher = FULL_CLASS_NAME_PATTERN.matcher(propertyTypeName);
            if (matcher.find()) {
                String realTypeName = matcher.group();
                isEntity = HEXAMON_ENTITY_CLASS_NAME.equals(realTypeName);
            }
        }
        return isEntity;
    }

    /**
     * Verify the given type matches at least one of the given class.
     *
     * @param type    type to check.
     * @param classes the classes to verfiy.
     * @return true if the type matches at least one of the given type.
     */
    static boolean doesTypeMatchAnyClass(Type type, Class<?>... classes) {
        String typeName = getFullClassName(type);
        return Arrays.stream(classes).anyMatch(c -> c.getName().equals(typeName));
    }

    /**
     * Extract the first inner type of the given type.
     *
     * @param genericType the input type.
     * @return The type of the first inner element of the generic type. Returns null if the input type is not generic.
     */
    static Type extractGenericFirstInnerType(Type genericType) {
        Type result = null;
        if (genericType instanceof ParameterizedType) {
            final Type[] innerTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            if (innerTypes.length > 0) {
                result = innerTypes[0];
            }
        }
        return result;
    }

}
