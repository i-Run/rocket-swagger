package fr.irun.openapi.swagger.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenAPIComponentsHelper<T> {
    private final Class<T> clazz;
    private final Function<Components, Map<String, T>> getter;
    private final BiConsumer<Components, Map<String, T>> setter;

    public Map<String, T> newMap() {
        return Maps.newHashMap();
    }

    public void mergeComponents(Components merged, Components... components) {
        Map<String, T> accu = Optional.ofNullable(getter.apply(merged)).orElseGet(Maps::newHashMap);

        for (Components component : components) {
            if (component == null) {
                continue;
            }
            Map<String, T> apply = Optional.ofNullable(getter.apply(component)).orElseGet(Maps::newHashMap);
            accu.putAll(apply);
        }
        if (!accu.isEmpty()) {
            setter.accept(merged, accu);
        }
    }

    public static final OpenAPIComponentsHelper<Schema> SCHEMAS =
            new OpenAPIComponentsHelper<>(Schema.class, Components::getSchemas, Components::setSchemas);
    public static final OpenAPIComponentsHelper<ApiResponse> API_RESPONSES =
            new OpenAPIComponentsHelper<>(ApiResponse.class, Components::getResponses, Components::setResponses);
    public static final OpenAPIComponentsHelper<Parameter> PARAMETERS =
            new OpenAPIComponentsHelper<>(Parameter.class, Components::getParameters, Components::setParameters);
    public static final OpenAPIComponentsHelper<Example> EXAMPLES =
            new OpenAPIComponentsHelper<>(Example.class, Components::getExamples, Components::setExamples);
    public static final OpenAPIComponentsHelper<RequestBody> REQUEST_BODIES =
            new OpenAPIComponentsHelper<>(RequestBody.class, Components::getRequestBodies, Components::setRequestBodies);
    public static final OpenAPIComponentsHelper<Header> HEADERS =
            new OpenAPIComponentsHelper<>(Header.class, Components::getHeaders, Components::setHeaders);
    public static final OpenAPIComponentsHelper<SecurityScheme> SECURITY_SCHEMES =
            new OpenAPIComponentsHelper<>(SecurityScheme.class, Components::getSecuritySchemes, Components::setSecuritySchemes);
    public static final OpenAPIComponentsHelper<Link> LINKS =
            new OpenAPIComponentsHelper<>(Link.class, Components::getLinks, Components::setLinks);
    public static final OpenAPIComponentsHelper<Callback> CALLBACKS =
            new OpenAPIComponentsHelper<>(Callback.class, Components::getCallbacks, Components::setCallbacks);
    public static final OpenAPIComponentsHelper<Object> EXTENSIONS =
            new OpenAPIComponentsHelper<>(Object.class, Components::getExtensions, Components::setExtensions);

    public static final ImmutableList<OpenAPIComponentsHelper<?>> ALL_COMPONENTS_TYPE = ImmutableList.of(
            SCHEMAS, API_RESPONSES, PARAMETERS, EXAMPLES, REQUEST_BODIES, HEADERS, SECURITY_SCHEMES, LINKS, CALLBACKS, EXTENSIONS
    );

    public static Components mergeAllComponents(Components... toBeMerge) {
        Components components = new Components();
        ALL_COMPONENTS_TYPE.forEach(openAPIComponentsHelper ->
                openAPIComponentsHelper.mergeComponents(components, toBeMerge));
        return components;
    }

    public static boolean isNullOrEmpty(Components components) {
        if (components == null) {
            return true;
        }
        return ALL_COMPONENTS_TYPE.stream()
                .allMatch(ct -> {
                    Map<String, ?> elements = ct.getter.apply(components);
                    return elements == null || elements.isEmpty();
                });
    }
}
