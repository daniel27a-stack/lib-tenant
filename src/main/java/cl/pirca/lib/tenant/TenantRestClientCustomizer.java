package cl.pirca.lib.tenant;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.web.client.RestClient;

/**
 * Registers the tenant propagation on every {@code RestClient.Builder} injected from Spring.
 */
public class TenantRestClientCustomizer implements RestClientCustomizer {

    private final TenantOutboundInterceptor interceptor = new TenantOutboundInterceptor();

    @Override
    public void customize(RestClient.Builder restClientBuilder) {
        restClientBuilder.requestInterceptor(interceptor);
    }
}
