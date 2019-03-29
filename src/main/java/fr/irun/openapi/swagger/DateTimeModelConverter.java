package fr.irun.openapi.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.utils.PropertyModelConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Model converter : include the default conversion and the management of the DateTime.
 */
final class DateTimeModelConverter implements ModelConverter {

    /**
     * Default instance used to convert a swagger property to a swagger model.
     */
    private final PropertyModelConverter propertyModelConverter;

    /**
     * Default ModelConverter used if this one does not manage a type.
     */
    private final ModelConverter baseConverter;

    DateTimeModelConverter() {
        this(new PropertyModelConverter(), new ModelResolver(new ObjectMapper()));
    }

    DateTimeModelConverter(PropertyModelConverter propertyModelConverter, ModelConverter modelConverter) {
        this.propertyModelConverter = propertyModelConverter;
        baseConverter = modelConverter;
    }


    @Override
    public Property resolveProperty(Type type, ModelConverterContext modelConverterContext,
                                    Annotation[] annotations, Iterator<ModelConverter> iterator) {
        Property property = null;
        if (type != null) {
            if (ModelConversionUtils.isDateType(type)) {
                property = new DateTimeProperty();
            } else {
                property = baseConverter.resolveProperty(type, modelConverterContext, annotations, iterator);
            }
        }
        return property;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        Property property = resolveProperty(type, modelConverterContext, null, iterator);
        Model model;

        if (property == null) {
            model = baseConverter.resolve(type, modelConverterContext, iterator);
        } else {
            model = propertyModelConverter.propertyToModel(property);
        }
        return model;
    }


}
