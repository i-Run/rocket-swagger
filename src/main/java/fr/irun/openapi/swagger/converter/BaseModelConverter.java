package fr.irun.openapi.swagger.converter;

import com.google.common.annotations.VisibleForTesting;
import fr.irun.openapi.swagger.utils.JacksonFactory;
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
import java.util.Optional;

/**
 * Model converter : include the default conversion and the management of the dates and not resolvable types.
 */
public final class BaseModelConverter implements ModelConverter {

    /**
     * Default ModelConverter used if this one does not manage a type.
     */
    private final ModelConverter baseConverter;

    public BaseModelConverter() {
        baseConverter = new ModelResolver(JacksonFactory.buildObjectMapper());
    }

    @VisibleForTesting
    BaseModelConverter(ModelConverter modelConverter) {
        baseConverter = modelConverter;
    }


    @Override
    public Property resolveProperty(Type type, ModelConverterContext modelConverterContext,
                                    Annotation[] annotations, Iterator<ModelConverter> iterator) {
        return Optional.of(type)
                .filter(ModelConversionUtils::isDateType)
                .<Property>map(t -> new DateTimeProperty())
                .orElseGet(() -> baseConverter.resolveProperty(type, modelConverterContext, annotations, iterator));
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        return baseConverter.resolve(type, modelConverterContext, iterator);
    }


}
