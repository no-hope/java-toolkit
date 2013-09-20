package org.nohope.reflection.test;

import org.junit.Test;
import org.nohope.reflection.IntrospectionUtils;
import org.nohope.reflection.ModifierMatcher;
import org.nohope.reflection.UtilitiesTestSupport;

import java.lang.reflect.InvocationTargetException;

import static org.nohope.reflection.IntrospectionUtils.invoke;
import static org.nohope.reflection.IntrospectionUtils.searchMethod;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/28/11 8:02 PM
 */
public final class NonPublicIntrospectionTest extends UtilitiesTestSupport {
    @Override
    public Class<?> getUtilityClass() {
        return IntrospectionUtils.class;
    }


    @Test
    public void nonPublicMethods()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        searchMethod(PackagePrivateChild.class,
                ModifierMatcher.ALL,
                "packageDefaultMethod");
        invoke(new PackagePrivateChild(), ModifierMatcher.ALL, "packageDefaultMethod");
        searchMethod(PackagePrivateChildWithOverrides.class,
                ModifierMatcher.ALL,
                "packageDefaultMethod");
        invoke(new PackagePrivateChildWithOverrides(), ModifierMatcher.ALL, "packageDefaultMethod");
    }
}
