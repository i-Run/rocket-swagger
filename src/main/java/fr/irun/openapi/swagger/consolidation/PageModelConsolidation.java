package fr.irun.openapi.swagger.consolidation;

import com.google.common.annotations.VisibleForTesting;
import fr.irun.openapi.swagger.converter.BaseModelConverter;
import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Optional;

public class PageModelConsolidation implements ModelConsolidation {

    private static final String MODEL_NAME = "Page";
    private static final String REFERENCE_PREFIX = "HexamonPage";

    private final ModelConverter baseConverter;

    private Type baseType;
    private ModelConverterContext context;
    private Annotation[] annotations;
    private Iterator<ModelConverter> converterChain;

    public PageModelConsolidation() {
        this.baseConverter = new BaseModelConverter();
    }

    @VisibleForTesting
    PageModelConsolidation(ModelConverter baseConverter) {
        this.baseConverter = baseConverter;
    }


    @Override
    public ModelEnum getModelType() {
        return ModelEnum.PAGE;
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
        final ModelConverter converter = Optional.ofNullable(converterChain)
                .filter(Iterator::hasNext)
                .map(Iterator::next)
                .orElse(baseConverter);

        final Property outProperty = converter.resolveProperty(baseType, context, annotations, converterChain);
        final String reference = ModelConversionUtils.getReference(outProperty).replace(MODEL_NAME, REFERENCE_PREFIX);
        ModelConversionUtils.setReference(outProperty, reference);
        return outProperty;
    }

    @Override
    public Model consolidateModel(Model model) {
        final ModelConverter converter = Optional.ofNullable(converterChain)
                .filter(Iterator::hasNext)
                .map(Iterator::next)
                .orElse(baseConverter);
        return converter.resolve(baseType, context, converterChain);
    }

}
