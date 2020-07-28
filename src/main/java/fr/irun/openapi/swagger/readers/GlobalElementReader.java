package fr.irun.openapi.swagger.readers;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
public final class GlobalElementReader {
    private final List<Parameter> parameters;
    private final List<SecurityRequirement> securityRequirements;
    private final Set<Tag> tags;
    private final List<Server> servers;
    private final Components components;

    public GlobalElementReader(OpenAPI openAPI) {
        this.parameters = new ArrayList<>();
        this.securityRequirements = new ArrayList<>();
        this.tags = new LinkedHashSet<>();
        this.servers = new ArrayList<>();
        this.components = Optional.ofNullable(openAPI)
                .map(OpenAPI::getComponents)
                .orElseGet(Components::new);
    }
}
