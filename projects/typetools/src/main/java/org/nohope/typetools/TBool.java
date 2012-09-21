package org.nohope.typetools;

import static org.nohope.reflection.IntrospectionUtils.cast;

/**
 * Date: 20.10.11
 * Time: 15:21
 */
public final class TBool {
    private TBool() {
    }

    public static boolean asBoolean(final Boolean bool) {
        return (bool != null) && bool;
    }

    public static boolean safeAsBoolean(final Object bool) {
        return cast(bool, Boolean.class, false);
    }
}
