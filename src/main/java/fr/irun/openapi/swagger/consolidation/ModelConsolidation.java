package fr.irun.openapi.swagger.consolidation;

import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Interface for the consolidation of a model and a property.
 */
public interface ModelConsolidation {

    /**
     * Obtain the model type related to this consolidation.
     *
     * @return the model type related to the consolidation.
     */
    ModelEnum getModelType();

    /**
     * Define the context of the consolidation.
     *
     * @param baseType       the base analyzed class.
     * @param context        the context of the conversion.
     * @param annotations    the annotaitons used to resolve the properties.
     * @param converterChain the chain of the model converters.
     */
    void setContext(Type baseType, ModelConverterContext context,
                    Annotation[] annotations, Iterator<ModelConverter> converterChain);

    /**
     * Consolidate a property.
     *
     * @param property the input property - can be null.
     * @return the consolidation property.
     */
    Property consolidateProperty(Property property);

    /**
     * Consolidate a model.
     *
     * @param model the model to consolidate - can be null.
     * @return the consolidation model.
     */
    Model consolidateModel(Model model);

}
