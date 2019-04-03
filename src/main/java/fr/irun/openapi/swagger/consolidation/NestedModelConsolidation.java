package fr.irun.openapi.swagger.consolidation;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import fr.irun.openapi.swagger.converter.DateTimeModelConverter;
import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ModelEnum;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Implementation of a consolidation for a Nested model.
 */
public class NestedModelConsolidation implements ModelConsolidation {

    private static final String NESTED_SUFFIX = "Nested";

    private final TypeFactory typeFactory;

    private final ModelConverter baseConverter;

    private Type nestedType;

    private ModelConverterContext context;

    private Annotation[] annotations;

    private Iterator<ModelConverter> converterIterator;

    public NestedModelConsolidation() {
        this(TypeFactory.defaultInstance(), new DateTimeModelConverter());
    }

    NestedModelConsolidation(TypeFactory typeFactory, ModelConverter baseConverter) {
        this.typeFactory = typeFactory;
        this.baseConverter = baseConverter;
    }


    @Override
    public ModelEnum getModelType() {
        return ModelEnum.NESTED;
    }

    @Override
    public void setContext(Type baseType, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> converterChain) {
        nestedType = baseType;
        this.context = context;
        converterIterator = converterChain;
        this.annotations = annotations;
    }

    @Override
    public Property consolidateProperty(Property property) {
        if (property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            refProperty.set$ref(refProperty.get$ref() + NESTED_SUFFIX);
        }
        return property;
    }

    @Override
    public Model consolidateModel(Model model) {
        final Type innerType = ModelConversionUtils.extractGenericFirstInnerType(nestedType);

        if (model != null && innerType != null) {
            JavaType innerJavaType = typeFactory.constructType(nestedType);
            Class<?> innerClass = typeFactory.constructType(innerType).getRawClass();

            ModelImpl outModel = ModelConversionUtils.copyModel(
                    innerClass.getSimpleName() + NESTED_SUFFIX,
                    model.getReference() + NESTED_SUFFIX,
                    model);

            Class<?> nestedClass = innerJavaType.getRawClass();
            fillModelWithNestedFields(outModel, nestedClass, innerJavaType, context, converterIterator);
            return outModel;
        }

        return model;
    }

    private void fillModelWithNestedFields(ModelImpl outModel, Class<?> nestedClass, JavaType innerJavaType,
                                           ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        Stream<Field> modelFields = Arrays.stream(nestedClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()));
        Type arrayType = typeFactory.constructArrayType(innerJavaType);

        final Property innerProperty = baseConverter.resolveProperty(innerJavaType, modelConverterContext, annotations, iterator);
        final Property arrayProperty = baseConverter.resolveProperty(arrayType, modelConverterContext, annotations, iterator);

        modelFields.forEach(field -> {
            final Class<?> fieldClass = field.getType();
            if (fieldClass.isArray() || Collection.class.isAssignableFrom(fieldClass)) {
                outModel.addProperty(field.getName(), innerProperty);

            } else {
                outModel.addProperty(field.getName(), arrayProperty);
            }
        });

    }
}