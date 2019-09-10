package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Factory for Jackson objects.
 */
public final class JacksonFactory {

    private JacksonFactory() {
    }

    /**
     * Build the object mapper with the required mixin.
     *
     * @return The object mapper to use for deserialization.
     */
    public static ObjectMapper buildObjectMapper() {
        return new ObjectMapper();
    }

}
