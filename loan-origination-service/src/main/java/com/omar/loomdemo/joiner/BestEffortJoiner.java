package com.omar.loomdemo.joiner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * A hand-written {@link Joiner} that waits for <strong>every</strong>
 * subtask to finish but never cancels the others just because one failed —
 * it collects whichever results succeeded and simply ignores the rest.
 *
 * <p>None of the built-in joiners fit this shape: {@code awaitAllSuccessfulOrThrow}
 * cancels the remaining subtasks the moment one fails, and
 * {@code awaitAll} still surfaces every exception it collected. Sending a
 * loan decision out over several notification channels is a case where a
 * failed SMS should never cancel the still-in-flight email — this Joiner
 * is exactly that "run everything, keep what worked" policy, expressed as
 * a reusable, independently testable class rather than inline logic.
 *
 * <p>Usage:
 * <pre>{@code
 * try (var scope = StructuredTaskScope.open(new BestEffortJoiner<NotificationResult>())) {
 *     scope.fork(() -> sendEmail(...));
 *     scope.fork(() -> sendSms(...));
 *     List<NotificationResult> delivered = scope.join();
 * }
 * }</pre>
 *
 * @param <T> the result type produced by each forked subtask
 */
public class BestEffortJoiner<T> implements Joiner<T, List<T>> {

    private final List<T> results = new ArrayList<>();

    /**
     * Called by the scope every time a subtask finishes. Returning
     * {@code false} tells the scope "don't cancel the others" regardless of
     * whether this subtask succeeded or failed — the defining behaviour of
     * this Joiner.
     *
     * @param subtask the subtask that just completed
     * @return always {@code false}, so sibling subtasks keep running
     */
    @Override
    public boolean onComplete(Subtask<? extends T> subtask) {
        if (subtask.state() == Subtask.State.SUCCESS) {
            synchronized (results) {
                results.add(subtask.get());
            }
        }
        // A failed subtask is silently dropped: its result simply won't
        // appear in the list returned by result(). Nothing to cancel for.
        return false;
    }

    /**
     * Called once by the scope after {@link StructuredTaskScope#join()}
     * returns, to produce the value handed back to the caller.
     *
     * @return the results of every subtask that completed successfully, in
     * completion order
     */
    @Override
    public List<T> result() {
        synchronized (results) {
            return List.copyOf(results);
        }
    }
}
