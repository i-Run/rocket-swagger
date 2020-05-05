package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.lang.reflect.Type;

public abstract class IgnoredTypes {
    public static final ImmutableSet<Type> IGNORED_REQUESTBODY_TYPES = ImmutableSet.of(
            TypeFactory.defaultInstance().constructType(ServerHttpRequest.class),
            TypeFactory.defaultInstance().constructType(org.springframework.http.server.ServerHttpRequest.class)
    );

    private IgnoredTypes() {
    }
}
