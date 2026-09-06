package com.omar.loomdemo.context;




import com.omar.loomdemo.exception.LoanOriginationException;

import java.lang.ScopedValue.CallableOp;
import java.util.UUID;

/**
 * Propagates a per-request correlation ID using Java's {@link ScopedValue}
 * API.
 *
 * <p>Unlike a {@link ThreadLocal}, a {@link ScopedValue} is immutable for the
 * duration of a bound call and is automatically and safely inherited by
 * Virtual Threads forked within a {@link java.util.concurrent.StructuredTaskScope},
 * which is exactly the pattern used by {@code LoanApplicationController} to
 * fan out calls to banking-data-service while keeping log lines traceable
 * back to a single request.
 */
public final class RequestContext {

    private static final ScopedValue<UUID> REQUEST_ID = ScopedValue.newInstance();

    private RequestContext() {
    }

    /**
     * Begins binding the given request ID to the current scope. Call
     * {@link Request#call(CallableOp)} on the result to actually run code
     * within that binding.
     *
     * @param requestId the correlation ID to bind for this request
     * @return a {@link Request} that can execute code with the ID bound
     */
    public static Request withRequestId(UUID requestId) {
        return new Request(ScopedValue.where(REQUEST_ID, requestId));
    }

    /**
     * Returns the request ID bound to the current scope.
     *
     * @return the current request ID
     * @throws LoanOriginationException if called outside a bound scope
     */
    public static UUID getRequestId() {
        return REQUEST_ID.orElseThrow(() -> new LoanOriginationException("No request ID available"));
    }

    /**
     * A pending scoped-value binding, ready to run a unit of work with the
     * request ID in scope.
     */
    public static final class Request {
        private final ScopedValue.Carrier carrier;

        private Request(ScopedValue.Carrier carrier) {
            this.carrier = carrier;
        }

        /**
         * Runs the given operation with the request ID bound as a scoped
         * value, visible to it and to any Virtual Threads it forks.
         *
         * @param callableOp the operation to run
         * @param <T>        the operation's result type
         * @param <X>        the checked exception type the operation may throw
         * @return the operation's result
         * @throws X if the operation throws
         */
        public <T, X extends Throwable> T call(CallableOp<T, X> callableOp) throws X {
            return carrier.call(callableOp);
        }
    }
}
