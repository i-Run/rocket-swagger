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
 * Consolidation for a mono instance. This class does not modify any model nor property.
 */
public class MonoModelConsolidation implements ModelConsolidation {

    @Override
    public ModelEnum getModelType() {
        return ModelEnum.MONO;
    }

    @Override
    public void setContext(Type baseType, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> converterChain) {
        // NOP
    }

    @Override
    public Property consolidateProperty(Property property) {
        // NOP
        return property;
    }

    @Override
    public Model consolidateModel(Model model) {
        // NOP
        return model;
    }
}
