package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantFilterTest {

    private final TenantFilter filter = new TenantFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void filtersAsyncDispatches() {
        assertThat(filter.shouldNotFilterAsyncDispatch()).isFalse();
    }

    @Test
    void orderIsTheDocumentedConstant() {
        assertThat(filter.getOrder()).isEqualTo(TenantFilter.ORDER);
    }

    @Test
    void setsTheTenantFromTheHeaderForTheWholeChain() throws Exception {
        request.addHeader("X-Tenant-Id", "acme");
        List<String> seen = new ArrayList<>();

        filter.doFilter(request, response, new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                seen.add(TenantContext.current().orElse("none"));
            }
        });

        assertThat(seen).containsExactly("acme");
        assertThat(TenantContext.current()).isEmpty();
    }

    @Test
    void proceedsWithNoTenantWhenTheHeaderIsMissing() throws Exception {
        assertNoTenantInChain();
    }

    @Test
    void proceedsWithNoTenantWhenTheHeaderIsBlank() throws Exception {
        request.addHeader("X-Tenant-Id", "   ");
        assertNoTenantInChain();
    }

    @Test
    void passesOnAnIoException() {
        request.addHeader("X-Tenant-Id", "acme");
        assertThatThrownBy(() -> filter.doFilter(request, response, (req, res) -> {
            throw new IOException("boom");
        })).isInstanceOf(IOException.class);
    }

    @Test
    void passesOnAServletException() {
        request.addHeader("X-Tenant-Id", "acme");
        assertThatThrownBy(() -> filter.doFilter(request, response, (req, res) -> {
            throw new ServletException("boom");
        })).isInstanceOf(ServletException.class);
    }

    @Test
    void passesOnARuntimeException() {
        request.addHeader("X-Tenant-Id", "acme");
        assertThatThrownBy(() -> filter.doFilter(request, response, (req, res) -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);
    }

    private void assertNoTenantInChain() throws Exception {
        List<Boolean> empty = new ArrayList<>();
        filter.doFilter(request, response, (req, res) -> empty.add(TenantContext.current().isEmpty()));
        assertThat(empty).containsExactly(true);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
