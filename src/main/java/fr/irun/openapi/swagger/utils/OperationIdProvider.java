package fr.irun.openapi.swagger.utils;

import com.google.common.collect.Sets;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class OperationIdProvider {
    private final Set<String> usedOperationIds;

    public OperationIdProvider() {
        usedOperationIds = Sets.newHashSet();
    }

    public OperationIdProvider load(OpenAPI openAPI) {
        Objects.requireNonNull(openAPI, "OpenAPI must not be null !");
        Paths paths = openAPI.getPaths();
        if (paths == null || paths.isEmpty()) {
            return this;
        }
        for (PathItem path : openAPI.getPaths().values()) {
            for (OpenApiHttpMethod value : OpenApiHttpMethod.values()) {
                Optional.ofNullable(value.pathItemGetter.apply(path))
                        .map(Operation::getOperationId)
                        .filter(StringUtils::isNotBlank)
                        .ifPresent(usedOperationIds::add);
            }
        }
        return this;
    }

    /**
     * Return the next available OperationId. If preferred operationId is not available we add counter at the end
     * with format `%s_%d`. Ex: `getUser_1`
     *
     * @param preferred The preferred OperationId if available
     * @return An unique OperationId
     */
    public String provideOperationId(String preferred) {
        String unusedOperationId = preferred;
        int counter = 0;
        while (usedOperationIds.contains(unusedOperationId)) {
            unusedOperationId = String.format("%s_%d", preferred, ++counter);
        }
        usedOperationIds.add(unusedOperationId);
        return unusedOperationId;
    }
}
