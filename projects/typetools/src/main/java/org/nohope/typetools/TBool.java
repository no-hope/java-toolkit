package org.nohope.typetools;

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
}
