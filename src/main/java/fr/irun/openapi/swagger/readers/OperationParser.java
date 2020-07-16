package fr.irun.openapi.swagger.readers;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

public final class OperationParser {
    private OperationParser() {
    }

    public static Optional<RequestBody> getRequestBody(
            io.swagger.v3.oas.annotations.parameters.RequestBody requestBody,
            RequestMapping classConsumes, RequestMapping methodConsumes, Components components, JsonView jsonViewAnnotation) {
        if (requestBody == null) {
            return Optional.empty();
        }
        RequestBody requestBodyObject = new RequestBody();
        boolean isEmpty = true;

        if (StringUtils.isNotBlank(requestBody.ref())) {
            requestBodyObject.set$ref(requestBody.ref());
            return Optional.of(requestBodyObject);
        }

        if (StringUtils.isNotBlank(requestBody.description())) {
            requestBodyObject.setDescription(requestBody.description());
            isEmpty = false;
        }
        if (requestBody.required()) {
            requestBodyObject.setRequired(requestBody.required());
            isEmpty = false;
        }
        if (requestBody.extensions().length > 0) {
            Map<String, Object> extensions = AnnotationsUtils.getExtensions(requestBody.extensions());
            for (String ext : extensions.keySet()) {
                requestBodyObject.addExtension(ext, extensions.get(ext));
            }
            isEmpty = false;
        }

        if (requestBody.content().length > 0) {
            isEmpty = false;
        }

        if (isEmpty) {
            return Optional.empty();
        }
        AnnotationsUtils.getContent(
                requestBody.content(), classConsumes == null ? new String[0] : classConsumes.value(),
                methodConsumes == null ? new String[0] : methodConsumes.value(),
                null, components, jsonViewAnnotation)
                .ifPresent(requestBodyObject::setContent);
        return Optional.of(requestBodyObject);
    }

    public static Optional<ApiResponses> getApiResponses(
            final io.swagger.v3.oas.annotations.responses.ApiResponse[] responses,
            RequestMapping classProduces, RequestMapping methodProduces, Components components, JsonView jsonViewAnnotation) {
        if (responses == null) {
            return Optional.empty();
        }
        ApiResponses apiResponsesObject = new ApiResponses();
        for (io.swagger.v3.oas.annotations.responses.ApiResponse response : responses) {
            ApiResponse apiResponseObject = new ApiResponse();
            if (StringUtils.isNotBlank(response.ref())) {
                apiResponseObject.set$ref(response.ref());
                if (StringUtils.isNotBlank(response.responseCode())) {
                    apiResponsesObject.addApiResponse(response.responseCode(), apiResponseObject);
                } else {
                    apiResponsesObject._default(apiResponseObject);
                }
                continue;
            }
            if (StringUtils.isNotBlank(response.description())) {
                apiResponseObject.setDescription(response.description());
            }
            if (response.extensions().length > 0) {
                Map<String, Object> extensions = AnnotationsUtils.getExtensions(response.extensions());
                for (String ext : extensions.keySet()) {
                    apiResponseObject.addExtension(ext, extensions.get(ext));
                }
            }

            AnnotationsUtils.getContent(response.content(),
                    classProduces == null ? new String[0] : classProduces.value(),
                    methodProduces == null ? new String[0] : methodProduces.value(),
                    null, components, jsonViewAnnotation)
                    .ifPresent(apiResponseObject::content);
            AnnotationsUtils.getHeaders(response.headers(), jsonViewAnnotation).ifPresent(apiResponseObject::headers);
            if (StringUtils.isNotBlank(apiResponseObject.getDescription())
                    || apiResponseObject.getContent() != null || apiResponseObject.getHeaders() != null) {

                Map<String, Link> links = AnnotationsUtils.getLinks(response.links());
                if (links.size() > 0) {
                    apiResponseObject.setLinks(links);
                }
                if (StringUtils.isNotBlank(response.responseCode())) {
                    apiResponsesObject.addApiResponse(response.responseCode(), apiResponseObject);
                } else {
                    apiResponsesObject._default(apiResponseObject);
                }
            }
        }

        if (apiResponsesObject.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(apiResponsesObject);
    }

    /**
     * Merge multiple {@link ApiResponses}
     *
     * @param apiResponses {@link ApiResponses} to merge, First declared override the next for the same response name
     * @return The merged {@link ApiResponses}
     */
    public static ApiResponses mergeApiResponses(ApiResponses... apiResponses) {
        ApiResponses newResponses = new ApiResponses();
        for (ApiResponses responses : apiResponses) {
            for (String responseName : responses.keySet()) {
                if (!newResponses.containsKey(responseName)) {
                    newResponses.put(responseName, responses.get(responseName));
                } else {
                    ApiResponse newResponse = newResponses.get(responseName);
                    ApiResponse response = responses.get(responseName);
                    if (newResponse != null && StringUtils.isBlank(newResponse.get$ref())) {
                        Content newContent = newResponse.getContent();
                        Content content = response.getContent();
                        if (newContent == null) {
                            newResponse.content(content);
                        } else {
                            for (String key : content.keySet()) {
                                if (newContent.get(key) == null) {
                                    newContent.addMediaType(key, content.get(key));
                                } else if (newContent.get(key).getSchema() == null) {
                                    newContent.get(key).setSchema(content.get(key).getSchema());
                                }
                            }
                        }
                    }
                }
            }
        }
        return newResponses;
    }

    public static boolean isSamePath(String path, String parentPath) {
        Path left = Optional.of(File.separator + path).map(Paths::get).orElse(Paths.get(""));
        Path right = Optional.of(File.separator + parentPath).map(Paths::get).orElse(Paths.get(""));

        return left.toAbsolutePath().equals(right.toAbsolutePath());
    }
}
