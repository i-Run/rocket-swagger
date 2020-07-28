package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Maps;
import fr.irun.openapi.swagger.utils.OpenApiHttpMethod;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public final class CallbackReader {

    private final OperationReader operationReader;

    public Map<String, Callback> readCallback(
            Method method, RequestMapping methodMapping, RequestMapping classMapping, JsonView jsonViewAnnotation) {
        List<io.swagger.v3.oas.annotations.callbacks.Callback> apiCallbacks =
                ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.callbacks.Callback.class);
        Map<String, Callback> callbacks = Maps.newLinkedHashMap();
        if (apiCallbacks != null) {
            for (io.swagger.v3.oas.annotations.callbacks.Callback methodCallback : apiCallbacks) {
                Map.Entry<String, Callback> currentCallbacks = this.readCallback(methodCallback, methodMapping, classMapping, jsonViewAnnotation);
                callbacks.put(currentCallbacks.getKey(), currentCallbacks.getValue());
            }
        }
        return callbacks;
    }

    public Map.Entry<String, Callback> readCallback(
            io.swagger.v3.oas.annotations.callbacks.Callback apiCallback,
            RequestMapping methodConsumes, RequestMapping classConsumes,
            JsonView jsonViewAnnotation) {

        Objects.requireNonNull(apiCallback, "API Callback can't be null !");

        Callback callbackObject = new Callback();
        if (StringUtils.isNotBlank(apiCallback.ref())) {
            callbackObject.set$ref(apiCallback.ref());
            return Maps.immutableEntry(apiCallback.name(), callbackObject);
        }
        PathItem pathItemObject = new PathItem();
        for (io.swagger.v3.oas.annotations.Operation callbackOperation : apiCallback.operation()) {
            Operation callbackOperationModel = operationReader.read(callbackOperation, methodConsumes, classConsumes, jsonViewAnnotation);
            pathItemObject = OpenApiHttpMethod.fromName(callbackOperation.method())
                    .pathItemSetter.apply(pathItemObject, callbackOperationModel);
        }

        callbackObject.addPathItem(apiCallback.callbackUrlExpression(), pathItemObject);

        return Maps.immutableEntry(apiCallback.name(), callbackObject);
    }

}
