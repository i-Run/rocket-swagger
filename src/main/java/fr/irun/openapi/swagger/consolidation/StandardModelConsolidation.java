package fr.irun.openapi.swagger.consolidation;

import fr.irun.openapi.swagger.converter.DateTimeModelConverter;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Model consolidation for a standard type.
 * This model does not consider the input property and model, it simply resolves the type using a given ModelConverter.
 */
public class StandardModelConsolidation implements ModelConsolidation {

    private final ModelConverter baseModelConverter;

    private Type baseType;

    private ModelConverterContext context;

    private Annotation[] annotations;

    private Iterator<ModelConverter> converterChain;

    public StandardModelConsolidation() {
        this(new DateTimeModelConverter());
    }

    StandardModelConsolidation(ModelConverter baseModelConverter) {
        this.baseModelConverter = baseModelConverter;
    }


    @Override
    public ModelEnum getModelType() {
        return ModelEnum.STANDARD;
    }

    @Override
    public void setContext(Type baseType, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> converterChain) {
        this.baseType = baseType;
        this.context = context;
        this.annotations = annotations;
        this.converterChain = converterChain;
    }

    @Override
    public Property consolidateProperty(Property property) {
        // Do not consider the input property - simply resolve with the base model converter.
        return baseModelConverter.resolveProperty(baseType, context, annotations, converterChain);
    }

    @Override
    public Model consolidateModel(Model model) {
        // Do not consider the input model - simply resolve with the base model converter.
        return baseModelConverter.resolve(baseType, context, converterChain);
    }
}
