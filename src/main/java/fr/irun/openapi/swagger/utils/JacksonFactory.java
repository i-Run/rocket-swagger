package fr.irun.openapi.swagger.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.irun.cms.api.model.Nested;
import fr.irun.cms.infra.config.jackson.NestedJacksonMixin;
import fr.irun.hexamon.api.entity.Entity;
import fr.irun.hexamon.infra.jackson.EntityJacksonMixin;

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
        final ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Entity.class, EntityJacksonMixin.class);
        mapper.addMixIn(Nested.class, NestedJacksonMixin.class);
        return mapper;
    }

}
