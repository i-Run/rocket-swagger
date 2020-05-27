package fr.irun.openapi.swagger.writers;

import com.google.common.base.Strings;
import fr.irun.openapi.swagger.readers.SecurityParser;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class SecuritySchemeWriter {
    private final OpenAPI output;

    public Map<String, SecurityScheme> write(Class<?> clazz) {
        if (output.getComponents() == null) {
            output.setComponents(new Components());
        }
        Components components = output.getComponents();

        List<io.swagger.v3.oas.annotations.security.SecurityScheme> apiSecurityScheme =
                ReflectionUtils.getRepeatableAnnotations(clazz, io.swagger.v3.oas.annotations.security.SecurityScheme.class);

        if (apiSecurityScheme != null) {
            for (io.swagger.v3.oas.annotations.security.SecurityScheme securitySchemeAnnotation : apiSecurityScheme) {
                SecurityParser.getSecurityScheme(securitySchemeAnnotation)
                        .filter(sc -> !Strings.isNullOrEmpty(sc.key))
                        .ifPresent(sc -> components.addSecuritySchemes(sc.key, sc.securityScheme));
            }
        }

        return components.getSecuritySchemes();
    }
}
