package fr.irun.openapi.swagger.utils;

import java.util.Arrays;

/**
 * Enumeration of the possible model types.
 */
public enum ModelEnum {

    /**
     * Hexamon Entity type.
     */
    ENTITY("fr.irun.hexamon.api.entity.Entity"),

    /**
     * {@link reactor.core.publisher.Flux}
     */
    FLUX("reactor.core.publisher.Flux"),

    /**
     * {@link reactor.core.publisher.Mono}
     */
    MONO("reactor.core.publisher.Mono"),

    /**
     * The model can be "Simply" resolved.
     */
    STANDARD("");

    private final String relatedClassName;

    ModelEnum(String relatedClassName) {
        this.relatedClassName = relatedClassName;
    }

    /**
     * Extract an enum from class name.
     *
     * @param className Name of the class to get the related type.
     * @return The type related to the given class.
     */
    public static ModelEnum fromClassName(String className) {
        return Arrays.stream(values())
                .filter(m -> m.relatedClassName.equals(className))
                .findFirst()
                .orElse(ModelEnum.STANDARD);
    }

}
