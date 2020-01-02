package fr.irun.openapi.swagger.resolver;

import fr.irun.openapi.swagger.utils.ResolutionStrategy;
import io.swagger.converter.ModelConverter;

/**
 * Interface for the resolver of a model and a property.
 */
public interface RocketModelResolver extends ModelConverter {

    /**
     * Obtain the strategy related to this resolver.
     *
     * @return the strategy related to the resolver.
     */
    ResolutionStrategy getResolutionStrategy();

}
