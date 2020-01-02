package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.type.TypeBase;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
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

    private ModelConversionUtils() {
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
     * Compute the resolution strategy to use for a type.
     *
     * @param inputType the type to get the strategy.
     * @return the strategy related to the type.
     */
    public static ResolutionStrategy getResolutionStrategy(Type inputType) {
        return ResolutionStrategy.fromClassName(getClassName(inputType));
    }

    /**
     * Extract the first inner type of the given type.
     *
     * @param genericType the input type.
     * @return The type of the first inner element of the generic type (as optional).
     */
    public static Optional<Type> extractGenericFirstInnerType(@Nullable Type genericType) {
        final VisitableGenericType.Visitor visitor = new VisitableGenericType.Visitor() {
            @Override
            public Optional<Type> getInnerTypeFromParameterizedType(ParameterizedType parameterizedType) {
                return Optional.of(parameterizedType)
                        .map(ParameterizedType::getActualTypeArguments)
                        .filter(array -> array.length > 0)
                        .map(array -> array[0]);
            }

            @Override
            public Optional<Type> getInnerTypeFromTypeBase(TypeBase typeBase) {
                return Optional.of(typeBase)
                        .map(TypeBase::getBindings)
                        .filter(b -> !b.isEmpty())
                        .map(b -> b.getBoundType(0));
            }

            @Override
            public Optional<Type> getInnerTypeFromDefaultType(Type type) {
                return Optional.empty();
            }
        };

        return wrapGenericType(genericType).getInnerType(visitor);
    }

    private static VisitableGenericType wrapGenericType(@Nullable Type genericType) {
        if (genericType instanceof ParameterizedType) {
            return VisitableParameterizedType.create((ParameterizedType) genericType);
        }
        if (genericType instanceof TypeBase) {
            return VisitableTypeBase.create((TypeBase) genericType);
        }
        return VisitableDefaultType.create(genericType);
    }

}
