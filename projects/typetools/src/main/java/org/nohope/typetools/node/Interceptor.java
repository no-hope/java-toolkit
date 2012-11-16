package org.nohope.typetools.node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/15/12 6:55 PM
 */
public interface Interceptor<N extends Node, R> extends Serializable {
    @Nullable R intercept(@Nonnull N node);
}
