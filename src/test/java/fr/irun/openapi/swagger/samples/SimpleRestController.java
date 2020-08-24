package fr.irun.openapi.swagger.samples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.assertj.core.util.Lists;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SimpleRestController {

    @Operation(description = "Description",
            responses = {
                    @ApiResponse(
                            responseCode = "401", description =
                            "- INVALID_PASSWORD -> The password associated to this account is invalid\n")
            })
    @GetMapping("/listStringsWithoutDefaultApiResponse")
    public List<String> listStringsWithoutDefaultApiResponse() {
        return Lists.emptyList();
    }

    @Operation(description = "Description",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "The default response description"),
                    @ApiResponse(
                            responseCode = "401", description =
                            "- INVALID_PASSWORD -> The password associated to this account is invalid\n")
            })
    @GetMapping("/listStringsWithIncompleteDefaultAdiResponse")
    public List<String> listStringsWithIncompleteDefaultAdiResponse() {
        return Lists.emptyList();
    }

    @GetMapping("/listStringsWithAnyAnnotations")
    public List<String> listStringsWithAnyAnnotations() {
        return Lists.emptyList();
    }

    @PostMapping("/listStringsWithAnyAnnotations")
    public List<String> listStringsWithAnyAnnotations(List<String> mylist) {
        return mylist;
    }
}
