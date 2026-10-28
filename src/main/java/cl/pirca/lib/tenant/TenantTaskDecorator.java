package cl.pirca.lib.tenant;

import org.springframework.core.task.TaskDecorator;

/**
 * Copies the tenant of the submitting thread to the thread that runs the task.
 */
public class TenantTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return TenantContext.current()
                .<Runnable>map(tenant -> () -> TenantContext.runAs(tenant, runnable))
                .orElse(runnable);
    }
}
