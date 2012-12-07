package org.nohope.bean;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 12/6/12 3:39 PM
 */
public abstract class AbstractDispatchable implements IDispatchable {
    private final IDispatcher dispatcher;

    protected AbstractDispatchable(@Nonnull final IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Nonnull
    @Override
    public final IDispatcher getDispatcher() {
        return dispatcher;
    }
}
