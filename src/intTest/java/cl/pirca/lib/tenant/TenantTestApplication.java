package cl.pirca.lib.tenant;

import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servlet application used by the integration tests: auto-configuration only, no executor defined.
 */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EnableAsync
class TenantTestApplication {

    @RestController
    static class ProbeController {

        private final AsyncProbe asyncProbe;

        ProbeController(AsyncProbe asyncProbe) {
            this.asyncProbe = asyncProbe;
        }

        @GetMapping("/api/ping")
        String ping() {
            return TenantContext.current().orElse("none");
        }

        @GetMapping("/health")
        String health() {
            return TenantContext.current().orElse("none");
        }

        @GetMapping("/api/async")
        String async() {
            return asyncProbe.tenant().join();
        }
    }

    @Component
    static class AsyncProbe {

        @Async
        CompletableFuture<String> tenant() {
            return CompletableFuture.completedFuture(TenantContext.current().orElse("none"));
        }
    }
}
