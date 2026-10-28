package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class TenantContextTest {

    @Test
    void hasNoTenantByDefault() {
        assertThat(TenantContext.current()).isEmpty();
    }

    @Test
    void runAsSetsTheTenantInsideTheBlockOnAPlainThread() throws InterruptedException {
        AtomicReference<String> inside = new AtomicReference<>();
        AtomicReference<String> after = new AtomicReference<>("unset");
        Thread thread = new Thread(() -> {
            TenantContext.runAs("acme", () -> inside.set(TenantContext.current().orElse(null)));
            after.set(TenantContext.current().orElse(null));
        });
        thread.start();
        thread.join();
        assertThat(inside.get()).isEqualTo("acme");
        assertThat(after.get()).isNull();
    }

    @Test
    void tenantIsNoLongerSetAfterTheBlock() {
        TenantContext.runAs("acme", () -> assertThat(TenantContext.current()).contains("acme"));
        assertThat(TenantContext.current()).isEmpty();
    }

    @Test
    void nestedRunAsRestoresTheOuterTenant() {
        TenantContext.runAs("acme", () -> {
            TenantContext.runAs("other", () -> assertThat(TenantContext.current()).contains("other"));
            assertThat(TenantContext.current()).contains("acme");
        });
    }

    @Test
    void tenantIsTrimmed() {
        TenantContext.runAs("  acme ", () -> assertThat(TenantContext.current()).contains("acme"));
    }

    @Test
    void runAsRejectsANullOrBlankTenant() {
        assertThatThrownBy(() -> TenantContext.runAs(null, () -> { })).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TenantContext.runAs("  ", () -> { })).isInstanceOf(IllegalArgumentException.class);
    }
}
