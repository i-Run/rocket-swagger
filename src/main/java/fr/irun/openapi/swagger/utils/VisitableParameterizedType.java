package fr.irun.openapi.swagger.utils;

import io.swagger.v3.core.converter.AnnotatedType;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

final class VisitableParameterizedType implements VisitableGenericType {

    private final ParameterizedType parameterizedType;

    private VisitableParameterizedType(ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    public static VisitableParameterizedType create(ParameterizedType parameterizedType) {
        return new VisitableParameterizedType(parameterizedType);
    }

    @Override
    public Optional<AnnotatedType> getInnerType(Visitor visitor) {
        return visitor.getInnerTypeFromParameterizedType(parameterizedType);
    }
}
