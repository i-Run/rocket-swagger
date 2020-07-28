package fr.irun.openapi.swagger.readers;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public final class OpenAPIExtensions {
    private static List<OpenAPIExtension> extensions;

    private OpenAPIExtensions() {
    }

    public static List<OpenAPIExtension> getExtensions() {
        return extensions;
    }

    public static void setExtensions(List<OpenAPIExtension> ext) {
        extensions = ext;
    }

    public static Iterator<OpenAPIExtension> chain() {
        return extensions.iterator();
    }

    static {
        extensions = new ArrayList<>();
        ServiceLoader<OpenAPIExtension> loader = ServiceLoader.load(OpenAPIExtension.class);
        for (OpenAPIExtension ext : loader) {
            log.debug("adding extension {}", ext);
            extensions.add(ext);
        }
        extensions.add(new DefaultParameterExtension());
    }
}