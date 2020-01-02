package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.type.TypeBase;

import java.lang.reflect.Type;
import java.util.Optional;

final class VisitableTypeBase implements VisitableGenericType {

    private final TypeBase typeBase;

    private VisitableTypeBase(TypeBase typeBase) {
        this.typeBase = typeBase;
    }

    public static VisitableTypeBase create(TypeBase typeBase) {
        return new VisitableTypeBase(typeBase);
    }

    @Override
    public Optional<Type> getInnerType(Visitor visitor) {
        return visitor.getInnerTypeFromTypeBase(typeBase);
    }
}
