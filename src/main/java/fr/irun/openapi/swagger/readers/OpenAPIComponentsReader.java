package fr.irun.openapi.swagger.readers;

import com.google.common.base.Strings;
import fr.irun.openapi.swagger.utils.OpenAPIComponentsHelper;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;
import java.util.Map;

public final class OpenAPIComponentsReader {
    private OpenAPIComponentsReader() {
    }

    public static Map<String, SecurityScheme> readSecuritySchemes(Class<?> clazz) {
        Map<String, SecurityScheme> schemes = OpenAPIComponentsHelper.SECURITY_SCHEMES.newMap();
        List<io.swagger.v3.oas.annotations.security.SecurityScheme> apiSecurityScheme =
                ReflectionUtils.getRepeatableAnnotations(clazz, io.swagger.v3.oas.annotations.security.SecurityScheme.class);

        if (apiSecurityScheme != null) {
            for (io.swagger.v3.oas.annotations.security.SecurityScheme securitySchemeAnnotation : apiSecurityScheme) {
                SecurityParser.getSecurityScheme(securitySchemeAnnotation)
                        .filter(sc -> !Strings.isNullOrEmpty(sc.key))
                        .ifPresent(sc -> schemes.put(sc.key, sc.securityScheme));
            }
        }

        return schemes;
    }
}
