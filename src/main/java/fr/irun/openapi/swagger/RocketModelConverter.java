package fr.irun.openapi.swagger;

import com.google.common.base.Functions;
import fr.irun.openapi.swagger.consolidation.EntityModelConsolidation;
import fr.irun.openapi.swagger.consolidation.FluxModelConsolidation;
import fr.irun.openapi.swagger.consolidation.ModelConsolidation;
import fr.irun.openapi.swagger.consolidation.MonoModelConsolidation;
import fr.irun.openapi.swagger.consolidation.NestedModelConsolidation;
import fr.irun.openapi.swagger.consolidation.StandardModelConsolidation;
import fr.irun.openapi.swagger.exceptions.RocketSwaggerException;
import fr.irun.openapi.swagger.utils.ModelConversionUtils;
import fr.irun.openapi.swagger.utils.ModelEnum;
import fr.irun.openapi.swagger.utils.ModelTypePair;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base model converter for the Rocket modules.
 */
public class RocketModelConverter implements ModelConverter {


    private final Map<ModelEnum, ModelConsolidation> consolidationMap;

    /**
     * Default constructor used by swagger-maven-plugin.
     */
    public RocketModelConverter() {
        this(Arrays.asList(
                new StandardModelConsolidation(),
                new MonoModelConsolidation(),
                new FluxModelConsolidation(),
                new NestedModelConsolidation(),
                new EntityModelConsolidation()
        ));
    }

    /**
     * For unit testing.
     *
     * @param consolidations all the consolidations to use..
     */
    RocketModelConverter(Collection<ModelConsolidation> consolidations) {
        this.consolidationMap = consolidations.stream()
                .collect(Collectors.toMap(ModelConsolidation::getModelType, Functions.identity()));
    }

    @Override
    public Property resolveProperty(Type type,
                                    ModelConverterContext modelConverterContext,
                                    Annotation[] annotations,
                                    Iterator<ModelConverter> iterator) {
        final List<Type> typesToResolve = ModelConversionUtils.extractInnerTypesReversed(type);
        checkTypesToResolve(type, typesToResolve);
        final List<ModelTypePair> modelTypePairList = computeModelTypesToConsolidate(typesToResolve);

        return modelTypePairList.stream()
                .reduce(null,
                        (outProperty, modelTypePair) -> {
                            final ModelConsolidation modelConsolidation = getConsolidationForModel(modelTypePair.model);
                            modelConsolidation.setContext(modelTypePair.type, modelConverterContext, annotations, iterator);
                            return modelConsolidation.consolidateProperty(outProperty);
                        },
                        (property1, property2) -> property2);
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        final List<Type> typesToResolve = ModelConversionUtils.extractInnerTypesReversed(type);
        checkTypesToResolve(type, typesToResolve);
        final List<ModelTypePair> modelTypePairList = computeModelTypesToConsolidate(typesToResolve);

        return modelTypePairList.stream()
                .reduce(null,
                        (outModel, modelTypePair) -> {
                            final ModelConsolidation modelConsolidation = getConsolidationForModel(modelTypePair.model);
                            modelConsolidation.setContext(modelTypePair.type, modelConverterContext, null, iterator);
                            return modelConsolidation.consolidateModel(outModel);
                        },
                        (model1, model2) -> model2);
    }

    private ModelConsolidation getConsolidationForModel(ModelEnum modelType) {

        return Optional.ofNullable(consolidationMap.get(modelType))
                .orElseThrow(() -> new RocketSwaggerException("Unable to find model consolidation for model type: " + modelType));
    }

    private void checkTypesToResolve(Type baseType, List<Type> typesToResolve) {

        Type type = typesToResolve.stream()
                .findFirst()
                .orElseThrow(() -> new RocketSwaggerException("A null input type is sent to resolving for RocketModelConverter."));

        final ModelEnum modelType = ModelConversionUtils.computeModelType(type);
        if (!ModelEnum.STANDARD.equals(modelType)) {
            throw new RocketSwaggerException("Non standard inner type for resolving of type: " + baseType + " - detected inner type: " + type);
        }
    }

    private List<ModelTypePair> computeModelTypesToConsolidate(List<Type> typesToResolve) {
        // Associate Model and Type into a Pair.
        return typesToResolve.stream()
                .map(type -> ModelTypePair.builder()
                        .model(ModelConversionUtils.computeModelType(type))
                        .type(type)
                        .build())
                .collect(Collectors.toList());
    }
}
