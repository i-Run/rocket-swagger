package fr.irun.openapi.swagger.utils;

/**
 * Enumeration of the possible model types:
 * <ul>
 * <li>FLUX</li>
 * <li>MONO</li>
 * <li>ENTITY</li>
 * <li>NESTED</li>
 * </ul>
 */
public enum ModelEnum {

    ENTITY,

    /**
     * {@see reactor.core.publisher.Flux}
     */
    FLUX,

    /**
     * {@see reactor.core.publisher.Mono}
     */
    MONO,

    NESTED,

    /**
     * The model can be "Simply" resolved.
     */
    STANDARD


}
