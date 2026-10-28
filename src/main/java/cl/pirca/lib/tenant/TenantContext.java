package cl.pirca.lib.tenant;

import java.util.Optional;

/**
 * The tenant (the customer that owns the data) of the current request or task.
 */
public final class TenantContext {

    /** Name of the HTTP header that carries the tenant between services. */
    public static final String HEADER_NAME = "X-Tenant-Id";

    private static final ScopedValue<String> TENANT = ScopedValue.newInstance();

    private TenantContext() {
    }

    /**
     * Returns the tenant of the current request or task, or empty when none is set.
     */
    public static Optional<String> current() {
        return TENANT.isBound() ? Optional.of(TENANT.get()) : Optional.empty();
    }

    /**
     * Runs the block with the given tenant set. When the block ends the tenant is no longer set.
     *
     * @throws IllegalArgumentException if the tenant is null or blank
     */
    public static void runAs(String tenant, Runnable block) {
        callAs(tenant, () -> {
            block.run();
            return null;
        });
    }

    static <R, X extends Throwable> R callAs(String tenant, ScopedValue.CallableOp<R, X> block) throws X {
        if (tenant == null || tenant.isBlank()) {
            throw new IllegalArgumentException("tenant must not be blank");
        }
        return ScopedValue.where(TENANT, tenant.trim()).call(block);
    }
}
