package fr.irun.openapi.swagger.utils;

import io.swagger.v3.core.converter.AnnotatedType;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;

final class VisitableDefaultType implements VisitableGenericType {

    @Nullable
    private final Type type;

    private VisitableDefaultType(@Nullable Type type) {
        this.type = type;
    }

    public static VisitableDefaultType create(@Nullable Type type) {
        return new VisitableDefaultType(type);
    }

    @Override
    public Optional<AnnotatedType> getInnerType(Visitor visitor) {
        return Optional.ofNullable(type)
                .flatMap(visitor::getInnerTypeFromDefaultType);
    }
}
