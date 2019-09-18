package fr.irun.openapi.swagger.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import fr.irun.openapi.swagger.model.AnyVisitableType;
import fr.irun.openapi.swagger.model.DateVisitableType;
import fr.irun.openapi.swagger.model.ResponseEntityVisitableType;
import fr.irun.openapi.swagger.model.VisitableType;
import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Model converter : include the default conversion and the management of the dates and not resolvable types.
 */
public final class BaseModelConverter implements ModelConverter {

    /**
     * Default ModelConverter used if this one does not manage a type.
     */
    private final ModelConverter baseConverter;

    public BaseModelConverter(ObjectMapper objectMapper) {
        baseConverter = new ModelResolver(objectMapper);
    }

    @VisibleForTesting
    BaseModelConverter(ModelConverter modelConverter) {
        baseConverter = modelConverter;
    }


    @Override
    public Property resolveProperty(Type type, ModelConverterContext modelConverterContext,
                                    Annotation[] annotations, Iterator<ModelConverter> iterator) {

        final VisitableType.Visitor<Property> propertyVisitor = new VisitableType.Visitor<Property>() {
            @Override
            public Property visitDateType(Type dateType) {
                return new DateTimeProperty();
            }

            @Override
            public Property visitResponseEntityType(Type responseEntityType) {
                final Type innerType = ModelConversionUtils.extractGenericFirstInnerType(responseEntityType);
                return baseConverter.resolveProperty(innerType, modelConverterContext, annotations, iterator);
            }

            @Override
            public Property visitAnyOtherType(Type baseType) {
                return baseConverter.resolveProperty(baseType, modelConverterContext, annotations, iterator);
            }
        };

        return wrapType(type).visit(propertyVisitor);
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        final VisitableType.Visitor<Model> modelVisitor = new VisitableType.Visitor<Model>() {
            @Override
            public Model visitDateType(Type dateType) {
                return baseConverter.resolve(dateType, modelConverterContext, iterator);
            }

            @Override
            public Model visitResponseEntityType(Type responseEntityType) {
                final Type innerType = ModelConversionUtils.extractGenericFirstInnerType(responseEntityType);
                return baseConverter.resolve(innerType, modelConverterContext, iterator);
            }

            @Override
            public Model visitAnyOtherType(Type baseType) {
                return baseConverter.resolve(baseType, modelConverterContext, iterator);
            }
        };

        return wrapType(type).visit(modelVisitor);
    }

    private VisitableType wrapType(Type baseType) {
        if (ModelConversionUtils.isDateType(baseType)) {
            return DateVisitableType.builder().type(baseType).build();
        }
        if (ModelConversionUtils.isResponseEntityType(baseType)) {
            return ResponseEntityVisitableType.builder().type(baseType).build();
        }
        return AnyVisitableType.builder().type(baseType).build();
    }

}
