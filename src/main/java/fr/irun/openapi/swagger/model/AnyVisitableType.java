package fr.irun.openapi.swagger.model;

import lombok.Builder;

import java.lang.reflect.Type;

/**
 * Default visitable type.
 */
@Builder
public final class AnyVisitableType implements VisitableType {

    private final Type type;

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitDefaultType(type);
    }
}
