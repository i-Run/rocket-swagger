package fr.irun.openapi.swagger.samples;

import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@SecurityScheme(name = HttpHeaders.AUTHORIZATION,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
@Tag(name = "security")
@RestController
public class AuthenticationController {
    @Operation(description =
            "Log a user using his username and his password. "
                    + "Return an accessToken and a refreshToken in the headers, and a list of the user authorities in the body",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            headers = {
                                    @Header(
                                            name = HttpHeaders.AUTHORIZATION,
                                            schema = @Schema(type = "string"),
                                            description = "Access token with 'Bearer ' prefix"),
                                    @Header(
                                            name = "X-Refresh-Token",
                                            schema = @Schema(type = "string"),
                                            description = "Refresh token with 'Bearer ' prefix")
                            },
                            description = "Authentication succeed, return a list of the user authorities in the body"),
                    @ApiResponse(
                            responseCode = "401", description =
                            "- INVALID_PASSWORD -> The password associated to this account is invalid\n"),
                    @ApiResponse(
                            responseCode = "403", description =
                            "- ACCOUNT_EXPIRED -> The user account is no more valid\n"
                                    + "- PERMISSION_DENIED -> The user does not have the authority `SECURITY_LOGIN_BACKOFFICE`\n"
                                    + "- SESSION_EXISTS -> Another session already exits for the user\n"),
                    @ApiResponse(responseCode = "417", description =
                            "- INVALID_FORM -> username: BAD_STRING_SIZE [ 30, 1 ]\n"
                                    + "- INVALID_FORM -> password: BAD_STRING_SIZE [ 30, 1 ]\n"
                                    + "- INVALID_USERNAME -> The username does not exist\n")
            })
    @PostMapping("/login")
    public Mono<ResponseEntity<ImmutableSet<String>>> login(@RequestBody @Valid Mono<LoginForm> loginForm, ServerHttpRequest request) {
        return Mono.empty();
    }

    @Operation(
            description = "Generate again the access token using a refresh token, and return a list of the user authorities in the body.",
            security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Refresh succeed, return a list of the user authorities in the body",
                            headers = {
                                    @Header(name = HttpHeaders.AUTHORIZATION,
                                            schema = @Schema(type = "string"),
                                            description = "Access token with 'Bearer ' prefix"),
                                    @Header(name = "X-Refresh-Token",
                                            schema = @Schema(type = "string"),
                                            description = "Refresh token with 'Bearer ' prefix")
                            }
                    ),
                    @ApiResponse(responseCode = "401", description =
                            "- INVALID_TOKEN -> The header refresh token is invalid\n"),
                    @ApiResponse(responseCode = "403", description =
                            "- ACCOUNT_EXPIRED -> The user account is no more valid\n"
                                    + "- TOKEN_EXPIRED -> The header refresh token has expired\n"
                                    + "- MULTIPLE_SESSION -> The user has opened another session, so the token is no more valid\n"
                                    + "- SESSION_WRONG_NETWORK -> The user session has been opened on another network than the current one\n")

            }
    )
    @PutMapping("/refresh")
    public Mono<ResponseEntity<ImmutableSet<String>>> refresh(ServerHttpRequest request) {
        return Mono.empty();
    }

    @Operation(
            description = "Check the expiration of the refreshToken and check if the user has changed network",
            security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)},
            responses = {
                    @ApiResponse(responseCode = "401", description =
                            "- INVALID_TOKEN -> The header access token is invalid\n"),
                    @ApiResponse(responseCode = "403", description =
                            "- TOKEN_EXPIRED -> The header access token has expired\n"
                                    + "- SESSION_WRONG_NETWORK -> The user session has been opened on another network than the current one\n")
            }
    )
    @GetMapping("/session/validate")
    public Mono<Void> validateSession() {
        return Mono.empty();
    }

    @Operation(
            description = "Delete a user session. You need to pass the refreshToken in the header",
            security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)},
            responses = {
                    @ApiResponse(responseCode = "401", description =
                            "- INVALID_TOKEN -> The header refresh token is invalid\n"),
                    @ApiResponse(responseCode = "403", description =
                            "- TOKEN_EXPIRED -> The header refresh token has expired\n")}
    )
    @DeleteMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerHttpRequest request) {
        return Mono.empty();
    }

    @Operation(
            description = "Obtain the user from access token",
            security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)},
            responses = {
                    @ApiResponse(responseCode = "401", description =
                            "- INVALID_TOKEN -> The header access token is invalid\n"),
                    @ApiResponse(responseCode = "403", description =
                            "- TOKEN_EXPIRED -> The header access token has expired\n")
            }
    )
    @GetMapping("/user/current")
    public Mono<User> getCurrentUser() {
        return Mono.empty();
    }


    @Operation(
            description = "Obtain the authorities available in the application",
            security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION, scopes = {"SECURITY_AUTHORITY_READ"})},
            responses = {
                    @ApiResponse(responseCode = "401", description =
                            "- INVALID_TOKEN -> The header access token is invalid\n"),
                    @ApiResponse(responseCode = "403", description =
                            "- TOKEN_EXPIRED -> The header access token has expired\n")
            }
    )
    @GetMapping("/authority/list")
    public Flux<User> getAvailableAuthorities() {
        return Flux.empty();
    }

    public static final class LoginForm {
        public final String username;
        public final String password;

        public LoginForm(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static final class User {
        public final String username;
        public final String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}
