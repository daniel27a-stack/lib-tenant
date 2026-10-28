package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.web.client.RestClient;

class TenantOutboundInterceptorTest {

    private final TenantOutboundInterceptor interceptor = new TenantOutboundInterceptor();

    @Test
    void addsTheHeaderWhenATenantIsSet() throws Exception {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/x"));
        TenantContext.runAs("acme", () -> send(request));
        assertThat(request.getHeaders().getFirst("X-Tenant-Id")).isEqualTo("acme");
    }

    @Test
    void addsNothingWithoutATenant() throws Exception {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/x"));
        send(request);
        assertThat(request.getHeaders().containsHeader("X-Tenant-Id")).isFalse();
    }

    @Test
    void theCustomizerRegistersTheInterceptor() {
        RestClient.Builder builder = RestClient.builder();
        new TenantRestClientCustomizer().customize(builder);
        builder.requestInterceptors(interceptors -> assertThat(interceptors).hasSize(1));
    }

    private void send(MockClientHttpRequest request) {
        try {
            interceptor.intercept(request, new byte[0], (req, body) -> null);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
