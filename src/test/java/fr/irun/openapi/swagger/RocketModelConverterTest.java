package fr.irun.openapi.swagger;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import fr.irun.openapi.swagger.resolver.FluxModelResolver;
import fr.irun.openapi.swagger.resolver.MonoModelResolver;
import fr.irun.openapi.swagger.resolver.RocketModelResolver;
import fr.irun.openapi.swagger.resolver.StandardModelResolver;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class RocketModelConverterTest {

    private static final Iterator<ModelConverter> ITERATOR = Iterators.forArray();
    private static final Annotation[] ANNOTATIONS = new Annotation[]{null};

    private RocketModelResolver fluxModelResolver;
    private RocketModelResolver monoModelResolver;
    private RocketModelResolver standardModelResolver;
    private ModelConverterContext context;

    private RocketModelConverter tested;

    @BeforeEach
    void setUp() {
        fluxModelResolver = mock(RocketModelResolver.class);
        monoModelResolver = mock(RocketModelResolver.class);
        standardModelResolver = mock(RocketModelResolver.class);
        context = mock(ModelConverterContext.class);

        // types
        when(fluxModelResolver.getModelType()).thenReturn(ModelEnum.FLUX);
        when(monoModelResolver.getModelType()).thenReturn(ModelEnum.MONO);
        when(standardModelResolver.getModelType()).thenReturn(ModelEnum.STANDARD);

        tested = new RocketModelConverter(
                Arrays.asList(fluxModelResolver, monoModelResolver, standardModelResolver));

        // Verify the constructor
        verify(fluxModelResolver).getModelType();
        verify(monoModelResolver).getModelType();
        verify(standardModelResolver).getModelType();
    }

    @Test
    @SuppressWarnings("unchecked")
    void defaultConstructor() {
        final String mapFieldName = "resolversMappedByType";
        final RocketModelConverter actualConverter = new RocketModelConverter();

        final Map<ModelEnum, RocketModelResolver> actualResolverMap = ReflectionUtils.readFieldValue(RocketModelConverter.class, mapFieldName, actualConverter)
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .orElseGet(() -> {
                    fail("No field with name '%s' found into default instance of %s", mapFieldName, RocketModelConverter.class);
                    return ImmutableMap.of();
                });

        assertThat(actualResolverMap).isNotNull();
        assertThat(actualResolverMap).hasSize(3);
        assertThat(actualResolverMap.values().stream().map(RocketModelResolver::getClass).map(Class::getName))
                .contains(
                        StandardModelResolver.class.getName(),
                        MonoModelResolver.class.getName(),
                        FluxModelResolver.class.getName()
                );
        assertThat(actualResolverMap.keySet()).contains(ModelEnum.STANDARD, ModelEnum.FLUX, ModelEnum.MONO);

        assertThat(actualResolverMap.entrySet().stream())
                .allMatch(e -> e.getKey().equals(e.getValue().getModelType()));
    }

    @Test
    void resolveProperty() {
        final Type inputType = mock(Type.class);
        final Property expectedProperty = mock(Property.class);
        // Flux
        {
            when(inputType.getTypeName()).thenReturn(buildTypeName(Flux.class));
            when(fluxModelResolver.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

            final Property actualProperty = tested.resolveProperty(inputType, context, ANNOTATIONS, ITERATOR);
            assertThat(actualProperty).isNotNull();
            assertThat(actualProperty).isSameAs(expectedProperty);

            verify(fluxModelResolver).resolveProperty(same(inputType), same(context), same(ANNOTATIONS), same(ITERATOR));
            verifyNoMoreInteractions(fluxModelResolver);
            verifyZeroInteractions(monoModelResolver, standardModelResolver);

            reset(inputType, fluxModelResolver, monoModelResolver, standardModelResolver);
        }
        // Mono
        {
            when(inputType.getTypeName()).thenReturn(buildTypeName(Mono.class));
            when(monoModelResolver.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

            final Property actualProperty = tested.resolveProperty(inputType, context, ANNOTATIONS, ITERATOR);
            assertThat(actualProperty).isNotNull();
            assertThat(actualProperty).isSameAs(expectedProperty);

            verify(monoModelResolver).resolveProperty(same(inputType), same(context), same(ANNOTATIONS), same(ITERATOR));
            verifyNoMoreInteractions(monoModelResolver);
            verifyZeroInteractions(fluxModelResolver, standardModelResolver);

            reset(inputType, fluxModelResolver, monoModelResolver, standardModelResolver);
        }
        // Standard
        {
            when(inputType.getTypeName()).thenReturn(buildTypeName(String.class));

            when(standardModelResolver.resolveProperty(any(), any(), any(), any())).thenReturn(expectedProperty);

            final Property actualProperty = tested.resolveProperty(inputType, context, ANNOTATIONS, ITERATOR);
            assertThat(actualProperty).isNotNull();
            assertThat(actualProperty).isSameAs(expectedProperty);

            verify(standardModelResolver).resolveProperty(same(inputType), same(context), same(ANNOTATIONS), same(ITERATOR));
            verifyNoMoreInteractions(standardModelResolver);
            verifyZeroInteractions(fluxModelResolver, monoModelResolver);

            reset(inputType, fluxModelResolver, monoModelResolver, standardModelResolver);
        }
    }

    @Test
    void resolve() {
        final Type inputType = mock(Type.class);
        final Model expectedModel = mock(Model.class);
        // Flux
        {
            when(inputType.getTypeName()).thenReturn(buildTypeName(Flux.class));
            when(fluxModelResolver.resolve(any(), any(), any())).thenReturn(expectedModel);

            final Model actualModel = tested.resolve(inputType, context, ITERATOR);
            assertThat(actualModel).isNotNull();
            assertThat(actualModel).isSameAs(expectedModel);

            verify(fluxModelResolver).resolve(same(inputType), same(context), same(ITERATOR));
            verifyNoMoreInteractions(fluxModelResolver);
            verifyZeroInteractions(monoModelResolver, standardModelResolver);

            reset(inputType, fluxModelResolver, monoModelResolver, standardModelResolver);
        }
        // Mono
        {
            when(inputType.getTypeName()).thenReturn(buildTypeName(Mono.class));
            when(monoModelResolver.resolve(any(), any(), any())).thenReturn(expectedModel);

            final Model actualModel = tested.resolve(inputType, context, ITERATOR);
            assertThat(actualModel).isNotNull();
            assertThat(actualModel).isSameAs(expectedModel);

            verify(monoModelResolver).resolve(same(inputType), same(context), same(ITERATOR));
            verifyNoMoreInteractions(monoModelResolver);
            verifyZeroInteractions(fluxModelResolver, standardModelResolver);

            reset(inputType, fluxModelResolver, monoModelResolver, standardModelResolver);
        }
        // Standard
        {
            when(inputType.getTypeName()).thenReturn(buildTypeName(String.class));
            when(standardModelResolver.resolve(any(), any(), any())).thenReturn(expectedModel);

            final Model actualModel = tested.resolve(inputType, context, ITERATOR);
            assertThat(actualModel).isNotNull();
            assertThat(actualModel).isSameAs(expectedModel);

            verify(standardModelResolver).resolve(same(inputType), same(context), same(ITERATOR));
            verifyNoMoreInteractions(standardModelResolver);
            verifyZeroInteractions(monoModelResolver, fluxModelResolver);

            reset(inputType, fluxModelResolver, monoModelResolver, standardModelResolver);
        }
    }

    private static String buildTypeName(Class<?> clazz) {
        return "[simple type, " + clazz + "]";
    }

}
