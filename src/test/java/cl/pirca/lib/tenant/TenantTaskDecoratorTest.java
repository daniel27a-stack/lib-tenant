package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class TenantTaskDecoratorTest {

    private final TenantTaskDecorator decorator = new TenantTaskDecorator();

    @Test
    void theTaskRunsWithTheTenantOfTheSubmitter() throws InterruptedException {
        AtomicReference<String> seen = new AtomicReference<>();
        Runnable[] decorated = new Runnable[1];
        TenantContext.runAs("acme", () -> decorated[0] = decorator.decorate(() -> seen.set(TenantContext.current().orElse(null))));

        Thread thread = new Thread(decorated[0]);
        thread.start();
        thread.join();

        assertThat(seen.get()).isEqualTo("acme");
    }

    @Test
    void withoutATenantTheTaskIsReturnedUnchanged() {
        Runnable task = () -> { };
        assertThat(decorator.decorate(task)).isSameAs(task);
    }
}
