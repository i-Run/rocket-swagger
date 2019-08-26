package fr.irun.openapi.swagger.model;

import lombok.Builder;

import java.lang.reflect.Type;

/**
 * Visitable type for any date type.
 */
@Builder
public class DateVisitableType extends AbstractVisitableType {

    public final Type type;

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitDateType(type);
    }
}
