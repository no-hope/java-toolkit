package org.nohope.util.typetools;

/**
 * Date: 20.10.11
 * Time: 15:21
 */
public final class BoolTools {
    private BoolTools() {
    }

    public static boolean asBoolean(final Boolean bool) {
        return (bool != null) && bool;
    }
}
