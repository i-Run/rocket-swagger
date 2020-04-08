package fr.irun.openapi.swagger;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.resolver.DateTimeModelResolver;
import fr.irun.openapi.swagger.resolver.DefaultModelResolver;
import fr.irun.openapi.swagger.resolver.GenericArrayModelResolver;
import fr.irun.openapi.swagger.resolver.GenericModelResolver;
import fr.irun.openapi.swagger.resolver.MapModelResolver;
import fr.irun.openapi.swagger.resolver.RocketModelResolver;
import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketModelConverterTest {

    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray();

    private ImmutableMap<ResolutionStrategy, RocketModelResolver> resolverMocks;

    private ModelConverterContext contextMock;

    private RocketModelConverter tested;

    @BeforeEach
    void setUp() {
        resolverMocks = ImmutableMap.of(
                ResolutionStrategy.WRAP_GENERIC_ARRAY, mock(RocketModelResolver.class),
                ResolutionStrategy.WRAP_GENERIC, mock(RocketModelResolver.class),
                ResolutionStrategy.DATE_TIME, mock(RocketModelResolver.class),
                ResolutionStrategy.MAP, mock(RocketModelResolver.class),
                ResolutionStrategy.DEFAULT, mock(RocketModelResolver.class)
        );
        contextMock = mock(ModelConverterContext.class);

        tested = new RocketModelConverter(resolverMocks);
    }

    @Test
    void should_create_default_resolver() {
        final RocketModelConverter actualConverter = new RocketModelConverter();

        final Map<ResolutionStrategy, RocketModelResolver> actualResolverMap = actualConverter.getResolversByStrategy();
        assertThat(actualResolverMap).isNotNull();
        assertThat(actualResolverMap).hasSize(5);

        assertThat(actualResolverMap.get(ResolutionStrategy.DATE_TIME)).isInstanceOf(DateTimeModelResolver.class);
        assertThat(actualResolverMap.get(ResolutionStrategy.WRAP_GENERIC_ARRAY)).isInstanceOf(GenericArrayModelResolver.class);
        assertThat(actualResolverMap.get(ResolutionStrategy.WRAP_GENERIC)).isInstanceOf(GenericModelResolver.class);
        assertThat(actualResolverMap.get(ResolutionStrategy.MAP)).isInstanceOf(MapModelResolver.class);
        assertThat(actualResolverMap.get(ResolutionStrategy.DEFAULT)).isInstanceOf(DefaultModelResolver.class);

        {
            final GenericModelResolver genericConverter = (GenericModelResolver) actualResolverMap.get(ResolutionStrategy.WRAP_GENERIC);
            assertThat(genericConverter.getBaseConverter()).isSameAs(actualConverter);
        }
        {
            final GenericArrayModelResolver genericConverter = (GenericArrayModelResolver) actualResolverMap.get(ResolutionStrategy.WRAP_GENERIC_ARRAY);
            assertThat(genericConverter.getBaseConverter()).isSameAs(actualConverter);
        }
    }

    private static Stream<Arguments> params_for_resolution() {
        return Stream.of(
                Arguments.of(ResolutionStrategy.DATE_TIME, Instant.class.getName()),
                Arguments.of(ResolutionStrategy.DATE_TIME, LocalDateTime.class.getName()),
                Arguments.of(ResolutionStrategy.DATE_TIME, java.util.Date.class.getName()),
                Arguments.of(ResolutionStrategy.DATE_TIME, java.sql.Date.class.getName()),
                Arguments.of(ResolutionStrategy.WRAP_GENERIC, Mono.class.getName()),
                Arguments.of(ResolutionStrategy.WRAP_GENERIC, "org.springframework.http.ResponseEntity<java.lang.Integer>"),
                Arguments.of(ResolutionStrategy.WRAP_GENERIC_ARRAY, Flux.class.getName()),
                Arguments.of(ResolutionStrategy.MAP, JsonNode.class.getName()),
                Arguments.of(ResolutionStrategy.DEFAULT, String.class.getName())
        );
    }


    @ParameterizedTest
    @MethodSource("params_for_resolution")
    void should_resolve_model(ResolutionStrategy strategy, String className) throws ClassNotFoundException {
        final RocketModelResolver resolverMock = resolverMocks.get(strategy);
        assertThat(resolverMock).withFailMessage("No resolver mock associated with strategy %s", strategy).isNotNull();

        final AnnotatedType inputType = new AnnotatedType(Class.forName(className));

        final Schema<?> expected = mock(Schema.class);
        when(resolverMock.resolve(inputType, contextMock, ITERATOR)).thenReturn(expected);

        final Schema<?> actual = tested.resolve(inputType, contextMock, ITERATOR);
        assertThat(actual).isNotNull();
        assertThat(actual).isSameAs(expected);

        verify(resolverMock).resolve(inputType, contextMock, ITERATOR);
        resolverMocks.values().forEach(Mockito::verifyNoMoreInteractions);
    }

}
