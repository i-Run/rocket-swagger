package fr.irun.openapi.swagger.samples;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(consumes = MediaType.APPLICATION_XML_VALUE)
public final class RestWithConsumesController {

    @PostMapping("/consumeSameAsClass")
    public Mono<Void> consumeSameAsClass(@RequestBody FormTest formtest) {
        return Mono.empty();
    }

    @PostMapping(value = "/consumeStream", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Void> consumeStream(@RequestBody Flux<FormTest> formtest) {
        return Mono.empty();
    }

    public static final class FormTest {
        public final String left;
        public final String right;

        public FormTest(String left, String right) {
            this.left = left;
            this.right = right;
        }
    }
}
