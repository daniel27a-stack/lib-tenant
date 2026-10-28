package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

@SpringBootTest(classes = TenantTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TenantAsyncIntTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TenantTestApplication.AsyncProbe asyncProbe;

    @Test
    void anAsyncMethodOfTheDefaultExecutorSeesTheTenantOfTheRequest() {
        String body = RestClient.create("http://localhost:" + port).get().uri("/api/async")
                .header("X-Tenant-Id", "acme").retrieve().body(String.class);
        assertThat(body).isEqualTo("acme");
    }

    @Test
    void controlWithoutATenantTheAsyncMethodSeesNone() {
        CompletableFuture<String> result = asyncProbe.tenant();
        assertThat(result.join()).isEqualTo("none");
    }
}
