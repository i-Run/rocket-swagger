package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.type.TypeBase;
import io.swagger.v3.core.converter.AnnotatedType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Visitable to get the first inner type of a generic type.
 */
interface VisitableGenericType {

    interface Visitor {
        Optional<AnnotatedType> getInnerTypeFromParameterizedType(ParameterizedType parameterizedType);

        Optional<AnnotatedType> getInnerTypeFromTypeBase(TypeBase typeBase);

        Optional<AnnotatedType> getInnerTypeFromDefaultType(Type type);
    }

    Optional<AnnotatedType> getInnerType(Visitor visitor);

}
