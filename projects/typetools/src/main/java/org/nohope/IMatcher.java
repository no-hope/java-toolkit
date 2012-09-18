package org.nohope;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/17/12 9:05 PM
 */
public interface IMatcher<T> {
    boolean matches(final T flags);
}
