package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@SpringBootTest(classes = TenantTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TenantInboundIntTest {

    @LocalServerPort
    private int port;

    private RestClient client() {
        return RestClient.builder().baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { })
                .build();
    }

    private ResponseEntity<String> get(String path, String tenant) {
        RestClient.RequestHeadersSpec<?> spec = client().get().uri(path);
        if (tenant != null) {
            spec = spec.header("X-Tenant-Id", tenant);
        }
        return spec.retrieve().toEntity(String.class);
    }

    @Test
    void theRouteSeesTheTenantOfTheHeader() {
        ResponseEntity<String> response = get("/api/ping", "acme");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("acme");
    }

    @Test
    void anApiRouteWithoutTenantAnswers400WithTenantRequired() {
        ResponseEntity<String> response = get("/api/ping", null);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getHeaders().getContentType().toString()).startsWith("application/problem+json");
        assertThat(response.getBody()).contains("\"code\":\"TENANT_REQUIRED\"").contains("\"status\":400");
    }

    @Test
    void anApiRouteWithABlankTenantAnswers400() {
        ResponseEntity<String> response = get("/api/ping", " ");
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("TENANT_REQUIRED");
    }

    @Test
    void aRouteOutsideApiIsAnsweredWithoutTenant() {
        ResponseEntity<String> response = get("/health", null);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("none");
    }
}
