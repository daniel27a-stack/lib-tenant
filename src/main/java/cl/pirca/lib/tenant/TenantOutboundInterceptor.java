package cl.pirca.lib.tenant;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Adds the {@code X-Tenant-Id} header with the current tenant to an outbound call, when one is set.
 */
class TenantOutboundInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        TenantContext.current().ifPresent(tenant -> request.getHeaders().set(TenantContext.HEADER_NAME, tenant));
        return execution.execute(request, body);
    }
}
