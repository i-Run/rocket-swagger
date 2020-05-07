package fr.irun.openapi.swagger.samples;


import io.swagger.v3.oas.annotations.Parameter;
import org.assertj.core.util.Lists;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public final class SimpleRestWithParameters {
    @GetMapping("/parameterWithoutAnnotation/{propertyPath}")
    public List<String> parameterWithoutAnnotation(@PathVariable String propertyPath) {
        return Lists.list(propertyPath);
    }

    @GetMapping("/parameterWithAnnotation/{propertyPath}")
    public List<String> parameterWithAnnotation(
            @Parameter(description = "The user name for login", required = true) @PathVariable String propertyPath) {
        return Lists.list(propertyPath);
    }

    @GetMapping("/namedParameterWithoutAnnotation/{propertyPath}")
    public List<String> namedParameterWithoutAnnotation(
            @PathVariable("propertyPath") String propertyPath) {
        return Lists.list(propertyPath);
    }

    @GetMapping("/namedParameterDiffWithoutAnnotation/{propertyPath}")
    public List<String> namedParameterDiffWithoutAnnotation(
            @PathVariable("propertyPath") String propPath) {
        return Lists.list(propPath);
    }

    @GetMapping("/namedParameterDiffWithAnnotation/{propertyPath}")
    public List<String> namedParameterDiffWithAnnotation(
            @Parameter(name = "propertyPath", description = "The user name for login", required = true)
            @PathVariable("propertyPath") String property) {
        return Lists.list(property);
    }
}
