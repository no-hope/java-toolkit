package org.nohope.cassandra.mapservice;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * All Iterables from cassandra should be once traversable.
 */
public final class OnceTraversableCIterable<T> implements Iterable<T> {

    private final AtomicBoolean iteratedFirstTime = new AtomicBoolean(true);
    private final Iterable<T> iterable;

    public OnceTraversableCIterable(@Nonnull final Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public Iterator<T> iterator() {
        if (iteratedFirstTime.getAndSet(false)) {
            return iterable.iterator();
        }
        throw new IllegalStateException("Can be iterated through only once");
    }
}
