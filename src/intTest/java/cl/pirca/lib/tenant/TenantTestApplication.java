package cl.pirca.lib.tenant;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        @GetMapping("/api/async-callable")
        Callable<String> asyncCallable() {
            return () -> "ok";
        }

        @GetMapping("/api/async-future")
        CompletableFuture<String> asyncFuture() {
            return CompletableFuture.completedFuture("ok");
        }

        @GetMapping("/api/async-deferred")
        DeferredResult<String> asyncDeferred() {
            DeferredResult<String> result = new DeferredResult<>();
            new Thread(() -> result.setResult("ok")).start();
            return result;
        }

        @GetMapping("/api/async")
        String async() {
            return asyncProbe.tenant().join();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class PostDispatchProbeConfig implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HandlerInterceptor() {
                @Override
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                    response.setHeader("X-Seen-Tenant", TenantContext.current().orElse("none"));
                    return true;
                }
            }).addPathPatterns("/api/async-*");
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
