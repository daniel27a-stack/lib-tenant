package cl.pirca.lib.tenant;

import cl.pirca.lib.errors.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Requires a tenant. Without one it throws a {@link BadRequestException} with code
 * {@code TENANT_REQUIRED}, which lib-errors turns into the answer.
 */
public class TenantRequiredInterceptor implements HandlerInterceptor {

    static final String CODE = "TENANT_REQUIRED";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (TenantContext.current().isEmpty()) {
            throw new BadRequestException(CODE, "The " + TenantContext.HEADER_NAME + " header is required.");
        }
        return true;
    }
}
