package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public final class SpringTypeResolver {
    private SpringTypeResolver() {
    }

    public static Type resolve(Type unresolvedType) {
        if (unresolvedType instanceof SimpleType) {
            return resolve((SimpleType) unresolvedType);
        }
        if (unresolvedType instanceof ParameterizedType) {
            return resolve((ParameterizedType) unresolvedType);
        }
        return unresolvedType;
    }

    public static Type resolve(SimpleType unresolvedType) {
        if ((unresolvedType.hasRawClass(Mono.class) || unresolvedType.hasRawClass(ResponseEntity.class))
                && !unresolvedType.getBindings().isEmpty()) {
            return unresolvedType.getBindings().getBoundType(0);
        } else if (unresolvedType.hasRawClass(Flux.class) && !unresolvedType.getBindings().isEmpty()) {
            return TypeFactory.defaultInstance().constructCollectionType(List.class, unresolvedType.getBindings().getBoundType(0));
        } else {
            return unresolvedType;
        }
    }

    public static Type resolve(ParameterizedType unresolvedType) {
        if ((unresolvedType.getRawType().equals(Mono.class) || unresolvedType.getRawType().equals(ResponseEntity.class))
                && unresolvedType.getActualTypeArguments().length == 1) {
            return resolve(unresolvedType.getActualTypeArguments()[0]);
        } else if (unresolvedType.getRawType().equals(Flux.class) && unresolvedType.getActualTypeArguments().length == 1) {
            TypeFactory typeFactory = TypeFactory.defaultInstance();
            Type actualTypeArgument = unresolvedType.getActualTypeArguments()[0];
            return resolve(typeFactory.constructCollectionType(List.class, typeFactory.constructType(actualTypeArgument)));
        } else {
            return unresolvedType;
        }
    }
}
