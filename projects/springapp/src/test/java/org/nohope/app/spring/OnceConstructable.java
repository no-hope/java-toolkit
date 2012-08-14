package org.nohope.app.spring;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/14/12 12:25 PM
 */
public class OnceConstructable {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final int id;

    private OnceConstructable(final int id) {
        this.id = id;
        COUNTER.addAndGet(1);
        if (COUNTER.get() > 1) {
            throw new IllegalStateException("Already constructed " + COUNTER.get() + " time(s)");
        }
    }

    public int getId() {
        return id;
    }
}
