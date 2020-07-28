package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

class SpringTypeResolverTest {
    @Test
    void should_resolve_parameterizedtype() {
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        {
            TypeReference<Mono<List<String>>> typeReference = new TypeReference<Mono<List<String>>>() {
            };
            Type actual = SpringTypeResolver.resolve((ParameterizedType) typeReference.getType());
            Assertions.assertThat(actual).isEqualTo(new TypeReference<List<String>>() {
            }.getType());
        }
        {
            TypeReference<Flux<List<String>>> typeReference = new TypeReference<Flux<List<String>>>() {
            };
            Type actual = SpringTypeResolver.resolve((ParameterizedType) typeReference.getType());
            Assertions.assertThat(actual).isEqualTo(typeFactory.constructType(new TypeReference<List<List<String>>>() {
            }.getType()));
        }
    }

    @Test
    void should_resolve_simpletype() {
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        {
            Type javaType = typeFactory.constructParametricType(Mono.class, String.class);
            Type actual = SpringTypeResolver.resolve((SimpleType) javaType);
            Assertions.assertThat(actual).isEqualTo(typeFactory.constructType(String.class));
        }
        {
            Type javaType = typeFactory.constructParametricType(Flux.class, String.class);
            Type actual = SpringTypeResolver.resolve((SimpleType) javaType);
            Assertions.assertThat(actual).isEqualTo(typeFactory.constructCollectionType(List.class, String.class));
        }
        {
            Type javaType = typeFactory.constructType(String.class);
            Type actual = SpringTypeResolver.resolve((SimpleType) javaType);
            Assertions.assertThat(actual).isEqualTo(typeFactory.constructType(String.class));
        }
    }
}