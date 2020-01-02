package fr.irun.openapi.swagger.utils;

import com.google.common.collect.ImmutableSet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Enumeration of the possible model types.
 */
public enum ResolutionStrategy {

    /**
     * Strategy for a generic wrapper (e.g. Mono or ResponseEntity).
     */
    WRAP_GENERIC("reactor.core.publisher.Mono"),

    /**
     * Strategy for a generic array wrapper (e.g. Flux).
     */
    WRAP_GENERIC_ARRAY("reactor.core.publisher.Flux"),

    /**
     * Strategy for a Date.
     */
    DATE_TIME(
            Instant.class.getName(),
            LocalDateTime.class.getName(),
            java.util.Date.class.getName(),
            java.sql.Date.class.getName()
    ),

    /**
     * Default strategy.
     */
    DEFAULT("");

    private final ImmutableSet<String> relatedClassNames;

    ResolutionStrategy(String... relatedClassNames) {
        this.relatedClassNames = ImmutableSet.copyOf(relatedClassNames);
    }

    /**
     * Extract an enum from class name.
     *
     * @param className Name of the class to get the related type.
     * @return The type related to the given class.
     */
    public static ResolutionStrategy fromClassName(String className) {
        return Arrays.stream(values())
                .filter(m -> m.relatedClassNames.contains(className))
                .findFirst()
                .orElse(ResolutionStrategy.DEFAULT);
    }

}
