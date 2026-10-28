package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class TenantAutoConfigurationTest {

    @Test
    void registersEveryPieceInAServletApplication() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TenantAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(TenantFilter.class);
                    assertThat(context).hasSingleBean(TenantRequiredInterceptor.class);
                    assertThat(context).hasSingleBean(TenantWebMvcConfigurer.class);
                    assertThat(context).hasSingleBean(TenantRestClientCustomizer.class);
                    assertThat(context).hasSingleBean(TenantTaskDecorator.class);
                });
    }
}
