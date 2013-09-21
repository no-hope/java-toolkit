package org.nohope.bean;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 12/7/12 7:48 PM
 */
public class TestDispatchable extends AbstractDispatchable {
    public TestDispatchable(@Nonnull final IDispatcher dispatcher) {
        super(dispatcher);
    }

    @Dispatch
    public void setSomething(final String test) {
    }

    @Dispatch
    public void set(final String test) {
    }

    @Dispatch(name = "test")
    public void set(final Integer test) {
    }

    @Dispatch(name = "test2")
    public int setXX(final Integer test) {
        return 0;
    }
}
