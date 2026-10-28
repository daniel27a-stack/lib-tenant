package cl.pirca.lib.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Sets the tenant from the {@code X-Tenant-Id} header for the whole request. It never rejects a
 * request: a request without the header proceeds with no tenant.
 */
public class TenantFilter extends OncePerRequestFilter implements Ordered {

    /** Order of the filter; any filter that needs the tenant must have a later (higher) order. */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * Runs the filter also on the dispatch that Spring MVC makes when asynchronous work (Callable,
     * CompletableFuture, DeferredResult) finishes, so the tenant is bound while the result is handled.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String tenant = request.getHeader(TenantContext.HEADER_NAME);
        if (tenant == null || tenant.isBlank()) {
            chain.doFilter(request, response);
            return;
        }
        try {
            TenantContext.<Void, Exception>callAs(tenant, () -> {
                chain.doFilter(request, response);
                return null;
            });
        } catch (ServletException | IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
