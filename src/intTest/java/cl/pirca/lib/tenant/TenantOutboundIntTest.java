package cl.pirca.lib.tenant;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootTest(classes = TenantTestApplication.class)
class TenantOutboundIntTest {

    interface Remote {
        @GetExchange("/remote")
        String call();
    }

    @Autowired
    private RestClient.Builder builder;

    @Test
    void aRestClientSendsTheTenantHeader() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        server.expect(requestTo("/remote")).andExpect(header("X-Tenant-Id", "acme")).andRespond(withSuccess("ok", null));

        TenantContext.runAs("acme", () -> client.get().uri("/remote").retrieve().body(String.class));

        server.verify();
    }

    @Test
    void anHttpInterfaceSendsTheTenantHeader() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Remote remote = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.build())).build()
                .createClient(Remote.class);
        server.expect(requestTo("/remote")).andExpect(header("X-Tenant-Id", "acme")).andRespond(withSuccess("ok", null));

        TenantContext.runAs("acme", remote::call);

        server.verify();
    }

    @Test
    void withoutATenantNoHeaderIsAdded() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        server.expect(requestTo("/remote")).andExpect(headerDoesNotExist("X-Tenant-Id")).andRespond(withSuccess("ok", null));

        client.get().uri("/remote").retrieve().body(String.class);

        server.verify();
    }
}
