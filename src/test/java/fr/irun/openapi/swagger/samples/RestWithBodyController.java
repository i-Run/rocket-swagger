package fr.irun.openapi.swagger.samples;

import com.google.common.collect.ImmutableSet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class RestWithBodyController {
    @PostMapping("/login")
    public Mono<ResponseEntity<ImmutableSet<String>>> login(@RequestBody @Valid Mono<LoginForm> loginForm) {
        return Mono.empty();
    }

    public static final class LoginForm {
        public final String username;
        public final String password;

        public LoginForm(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
