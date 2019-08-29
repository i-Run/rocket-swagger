package fr.irun.openapi.swagger.model;

import lombok.Builder;

import java.lang.reflect.Type;

/**
 * Visitable type for Spring org.springframework.http.ResponseEntity.
 * ResponseEntity wraps the whole content of the HTTP response, whereas swagger only needs to resolve the type of the body.
 */
@Builder
public final class ResponseEntityVisitableType implements VisitableType {

    private final Type type;

    @Override
    public <T> T visit(Visitor<T> visitor) {
        return visitor.visitResponseEntityType(type);
    }
}
