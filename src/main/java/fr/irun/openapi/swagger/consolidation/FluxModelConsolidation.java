package fr.irun.openapi.swagger.consolidation;

import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Optional;

/**
 * Implementation used to consolidate a model used into a reactor Flux.
 */
public class FluxModelConsolidation implements ModelConsolidation {


    @Override
    public ModelEnum getModelType() {
        return ModelEnum.FLUX;
    }

    @Override
    public void setContext(Type baseType, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> converterChain) {
        // NOP: the context is used before entering the Flux consolidation.
    }

    @Override
    public Property consolidateProperty(Property property) {
        return Optional.ofNullable(property)
                .map(ArrayProperty::new)
                .orElse(null);
    }

    @Override
    public Model consolidateModel(Model model) {
        // No update to perform on the model.
        return model;
    }
}



