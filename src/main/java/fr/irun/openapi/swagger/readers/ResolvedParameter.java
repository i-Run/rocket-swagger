package fr.irun.openapi.swagger.readers;

import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public final class ResolvedParameter {
    public static final ResolvedParameter EMPTY = new ResolvedParameter(
            Collections.emptyList(), null, Collections.emptyList());

    private final List<Parameter> parameters;
    private final Parameter requestBody;
    private final List<Parameter> formParameters;
}
