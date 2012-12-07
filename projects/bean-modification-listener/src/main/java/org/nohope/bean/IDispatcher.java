package org.nohope.bean;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 12/6/12 3:38 PM
 */
public interface IDispatcher {
    void handle(@Nonnull final IDispatchable obj,
                @Nonnull final String propertyName,
                final Object newValue);
}
