package org.nohope.reflection;

import org.nohope.IMatcher;

import javax.annotation.Nonnull;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 9/13/12 1:11 AM
*/
public interface IModifierMatcher extends IMatcher<Integer> {
    @Override
    boolean matches(@Nonnull final Integer flags);
}
