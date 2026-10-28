package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.pirca.lib.errors.BadRequestException;
import cl.pirca.lib.errors.PircaException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantRequiredInterceptorTest {

    private final TenantRequiredInterceptor interceptor = new TenantRequiredInterceptor();

    @Test
    void rejectsWithTenantRequiredWhenNoTenantIsSet() {
        assertThatThrownBy(() -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BadRequestException.class)
                .extracting(e -> ((PircaException) e).getCode())
                .isEqualTo("TENANT_REQUIRED");
    }

    @Test
    void letsTheRequestPassWhenATenantIsSet() {
        TenantContext.runAs("acme", () -> assertThat(
                interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object())).isTrue());
    }
}
