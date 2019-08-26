package fr.irun.openapi.swagger.model;

import lombok.Builder;

import java.lang.reflect.Type;

/**
 * Visitable type for Spring {@link org.springframework.http.ResponseEntity}.
 */
@Builder
public class ResponseEntityVisitableType implements VisitableType {

    private final Type type;

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitResponseEntityType(type);
    }
}
