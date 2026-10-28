package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.ServletRequestPathUtils;

class TenantWebMvcConfigurerTest {

    @Test
    void registersTheInterceptorForApiRoutesOnly() {
        TenantRequiredInterceptor interceptor = new TenantRequiredInterceptor();
        Registry registry = new Registry();

        new TenantWebMvcConfigurer(interceptor).addInterceptors(registry);

        MappedInterceptor mapped = (MappedInterceptor) registry.registered().getFirst();
        assertThat(mapped.getInterceptor()).isSameAs(interceptor);
        assertThat(matches(mapped, "/api/ping")).isTrue();
        assertThat(matches(mapped, "/health")).isFalse();
    }

    private static boolean matches(MappedInterceptor mapped, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        ServletRequestPathUtils.parseAndCache(request);
        return mapped.matches(request);
    }

    private static final class Registry extends InterceptorRegistry {
        List<Object> registered() {
            return getInterceptors();
        }
    }
}
