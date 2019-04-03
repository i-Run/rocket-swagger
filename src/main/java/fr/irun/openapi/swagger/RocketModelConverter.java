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
        List<Type> typesToResolve = ModelConversionUtils.extractInnerTypesReversed(type);
        checkTypesToResolve(type, typesToResolve);
        List<ModelTypePair> modelTypePairList = computeModelTypesToConsolidate(typesToResolve);

        Property outProperty = null;
        for (ModelTypePair pair : modelTypePairList) {
            final ModelConsolidation modelConsolidation = getConsolidationForModel(pair.model);
            modelConsolidation.setContext(pair.type, modelConverterContext, annotations, iterator);
            outProperty = modelConsolidation.consolidateProperty(outProperty);
        }
        return outProperty;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        List<Type> typesToResolve = ModelConversionUtils.extractInnerTypesReversed(type);
        checkTypesToResolve(type, typesToResolve);
        List<ModelTypePair> modelTypePairList = computeModelTypesToConsolidate(typesToResolve);

        Model outModel = null;
        for (ModelTypePair pair : modelTypePairList) {
            final ModelConsolidation modelConsolidation = getConsolidationForModel(pair.model);
            modelConsolidation.setContext(pair.type, modelConverterContext, null, iterator);
            outModel = modelConsolidation.consolidateModel(outModel);
        }
        return outModel;
    }

    private ModelConsolidation getConsolidationForModel(ModelEnum modelType) {
        ModelConsolidation modelConsolidation = this.consolidationMap.get(modelType);
        if (modelConsolidation == null) {
            throw new RocketSwaggerException("Unable to find model consolidation for model type: " + modelType);
        }
        return modelConsolidation;
    }

    private void checkTypesToResolve(Type baseType, List<Type> typesToResolve) {
        if (typesToResolve.isEmpty()) {
            throw new RocketSwaggerException("A null input type is sent to resolving for RocketModelConverter.");
        }
        final Type type = typesToResolve.get(0);
        if (!ModelEnum.STANDARD.equals(ModelConversionUtils.computeModelType(type))) {
            throw new RocketSwaggerException("Non standard inner type for resolving of type: " + baseType
                    + " - detected inner type: " + type);
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
