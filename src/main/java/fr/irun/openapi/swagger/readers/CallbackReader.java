package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.irun.openapi.swagger.utils.OpenApiHttpMethod;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public final class CallbackReader {
    private final OperationReader operationReader;

    public Map<String, Callback> readCallbacks(
            io.swagger.v3.oas.annotations.callbacks.Callback apiCallback,
            RequestMapping methodConsumes, RequestMapping classConsumes, JsonView jsonViewAnnotation) {

        Map<String, Callback> callbackMap = new HashMap<>();
        if (apiCallback == null) {
            return callbackMap;
        }

        Callback callbackObject = new Callback();
        if (StringUtils.isNotBlank(apiCallback.ref())) {
            callbackObject.set$ref(apiCallback.ref());
            callbackMap.put(apiCallback.name(), callbackObject);
            return callbackMap;
        }
        PathItem pathItemObject = new PathItem();
        for (io.swagger.v3.oas.annotations.Operation callbackOperation : apiCallback.operation()) {
            Operation callbackOperationModel = operationReader.read(callbackOperation, methodConsumes, classConsumes, jsonViewAnnotation);
            pathItemObject = OpenApiHttpMethod.fromName(callbackOperation.method())
                    .pathItemSetter.apply(pathItemObject, callbackOperationModel);
        }

        callbackObject.addPathItem(apiCallback.callbackUrlExpression(), pathItemObject);
        callbackMap.put(apiCallback.name(), callbackObject);

        return callbackMap;
    }

}
