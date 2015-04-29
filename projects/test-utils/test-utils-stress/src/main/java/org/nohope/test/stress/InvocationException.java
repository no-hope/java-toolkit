package org.nohope.test.stress;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
* @since 2013-12-29 18:39
*/
public class InvocationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final long startNanos;
    private final long endNanos;

    public InvocationException(final Exception e,
                               final long startNanos,
                               final long endNanos) {
        super(e);
        this.startNanos = startNanos;
        this.endNanos = endNanos;
    }

    public long getStartNanos() {
        return startNanos;
    }

    public long getEndNanos() {
        return endNanos;
    }
}
