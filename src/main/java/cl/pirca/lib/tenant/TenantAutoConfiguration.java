package cl.pirca.lib.tenant;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.client.RestClient;

/**
 * Registers the tenant pieces in servlet web applications.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = Type.SERVLET)
public class TenantAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    static class Inbound {

        @Bean
        @ConditionalOnMissingBean
        TenantFilter tenantFilter() {
            return new TenantFilter();
        }

        @Bean
        @ConditionalOnMissingBean
        TenantRequiredInterceptor tenantRequiredInterceptor() {
            return new TenantRequiredInterceptor();
        }

        @Bean
        @ConditionalOnMissingBean
        TenantWebMvcConfigurer tenantWebMvcConfigurer(TenantRequiredInterceptor interceptor) {
            return new TenantWebMvcConfigurer(interceptor);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({RestClient.class, RestClientCustomizer.class})
    static class Outbound {

        @Bean
        @ConditionalOnMissingBean
        TenantRestClientCustomizer tenantRestClientCustomizer() {
            return new TenantRestClientCustomizer();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Async {

        @Bean
        @ConditionalOnMissingBean(TaskDecorator.class)
        TenantTaskDecorator tenantTaskDecorator() {
            return new TenantTaskDecorator();
        }
    }
}
