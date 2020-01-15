package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.type.TypeBase;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Visitable to get the first inner type of a generic type.
 */
interface VisitableGenericType {

    interface Visitor {
        Optional<Type> getInnerTypeFromParameterizedType(ParameterizedType parameterizedType);

        Optional<Type> getInnerTypeFromTypeBase(TypeBase typeBase);

        Optional<Type> getInnerTypeFromDefaultType(Type type);
    }

    Optional<Type> getInnerType(Visitor visitor);

}
