package fr.irun.openapi.swagger.model;

import lombok.Builder;

import java.lang.reflect.Type;

/**
 * Visitable type for any date type.
 */
@Builder
public final class DateVisitableType implements VisitableType {

    public final Type type;

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitDateType(type);
    }
}
