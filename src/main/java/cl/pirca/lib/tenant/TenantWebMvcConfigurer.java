package cl.pirca.lib.tenant;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Applies the tenant-required check to {@code /api/**} only.
 */
public class TenantWebMvcConfigurer implements WebMvcConfigurer {

    static final String PATTERN = "/api/**";

    private final TenantRequiredInterceptor interceptor;

    public TenantWebMvcConfigurer(TenantRequiredInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns(PATTERN);
    }
}
