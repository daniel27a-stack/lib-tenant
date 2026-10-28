package cl.pirca.lib.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

class TenantAutoConfigurationIntTest {

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TenantAutoConfiguration.class));

    @Test
    void nothingIsRegisteredInANonWebApplication() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(TenantAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TenantFilter.class);
                    assertThat(context).doesNotHaveBean(TenantRequiredInterceptor.class);
                    assertThat(context).doesNotHaveBean(TenantRestClientCustomizer.class);
                    assertThat(context).doesNotHaveBean(TenantTaskDecorator.class);
                    assertThat(context).doesNotHaveBean(TenantWebMvcConfigurer.class);
                });
    }

    @Test
    void everyPieceBacksOffWhenTheApplicationDefinesItsOwn() {
        webRunner.withUserConfiguration(Own.class).run(context -> {
            assertThat(context).hasSingleBean(TenantFilter.class);
            assertThat(context.getBean(TenantFilter.class)).isSameAs(context.getBean("ownFilter"));
            assertThat(context.getBean(TenantRequiredInterceptor.class)).isSameAs(context.getBean("ownInterceptor"));
            assertThat(context.getBean(TenantRestClientCustomizer.class)).isSameAs(context.getBean("ownCustomizer"));
            assertThat(context).hasSingleBean(TaskDecorator.class);
            assertThat(context.getBean(TaskDecorator.class)).isSameAs(context.getBean("ownDecorator"));
            assertThat(context).hasSingleBean(TenantWebMvcConfigurer.class);
            assertThat(context.getBean(TenantWebMvcConfigurer.class)).isSameAs(context.getBean("ownConfigurer"));
        });
    }

    @Test
    void theImportsFileListsOnlyTheTenantAutoConfiguration() {
        assertThat(ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader()).getCandidates())
                .filteredOn(name -> name.startsWith("cl.pirca.lib.tenant."))
                .containsExactly(TenantAutoConfiguration.class.getName());
    }

    @Configuration(proxyBeanMethods = false)
    static class Own {

        @Bean
        TenantFilter ownFilter() {
            return new TenantFilter();
        }

        @Bean
        TenantRequiredInterceptor ownInterceptor() {
            return new TenantRequiredInterceptor();
        }

        @Bean
        TenantRestClientCustomizer ownCustomizer() {
            return new TenantRestClientCustomizer();
        }

        @Bean
        TaskDecorator ownDecorator() {
            return runnable -> runnable;
        }

        @Bean
        TenantWebMvcConfigurer ownConfigurer() {
            return new TenantWebMvcConfigurer(new TenantRequiredInterceptor());
        }
    }
}
