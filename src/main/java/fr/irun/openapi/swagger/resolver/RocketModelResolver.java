package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;

/**
 * Interface for the resolver of a model and a property.
 */
public interface RocketModelResolver extends ModelConverter {

    /**
     * Obtain the model type related to this resolver.
     *
     * @return the model type related to the resolver.
     */
    ModelEnum getModelType();

}
