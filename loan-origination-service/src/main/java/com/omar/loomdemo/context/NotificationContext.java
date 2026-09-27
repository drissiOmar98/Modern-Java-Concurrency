package com.omar.loomdemo.context;

/**
 * Binds a "best-effort" flag as a {@link ScopedValue}, independent of the
 * request-ID binding in {@link RequestContext}.
 *
 * <p>This demonstrates two things {@code RequestContext} alone doesn't:
 * <ul>
 *   <li>Multiple, independently-scoped values can be active at once — the
 *       request ID stays bound for the whole request, while this flag is
 *       bound only around the narrower notification-dispatch step.</li>
 *   <li>A scope can be nested inside another. Any code (and any subtask
 *       forked from a {@code StructuredTaskScope}) running inside
 *       {@link #run} sees both the outer request ID <em>and</em> this
 *       flag, with no manual plumbing required.</li>
 * </ul>
 *
 * <p>{@code NotificationService} reads this flag to decide whether a
 * channel failure should be logged and swallowed (best-effort) or rethrown.
 */
public final class NotificationContext {

    private static final ScopedValue<Boolean> BEST_EFFORT = ScopedValue.newInstance();

    private NotificationContext() {
    }

    /**
     * Runs {@code action} with the best-effort flag bound to {@code true},
     * nested inside whatever scope is already active (typically a
     * {@link RequestContext} request-ID binding).
     *
     * @param action the operation to run with the flag bound
     */
    public static void run(Runnable action) {
        ScopedValue.where(BEST_EFFORT, true).run(action);
    }

    /**
     * @return {@code true} if the current code is running inside a
     * {@link #run} block, {@code false} otherwise (e.g. a direct call made
     * outside of notification dispatch, where failures should not be
     * silently swallowed)
     */
    public static boolean isBestEffort() {
        return BEST_EFFORT.orElse(false);
    }
}
