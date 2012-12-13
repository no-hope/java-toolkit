package org.nohope.typetools;

import javax.annotation.Nullable;

/**
 * Date: 17.10.11
 * Time: 14:48
 */
public final class TDouble {
    private TDouble() {
    }

    public static boolean isDoubleCorrect(final Double val) {
        return null != val && !(val.isNaN() || val.isInfinite());
    }

    public static double asDouble(final Double val, final Double defValue) {
        return isDoubleCorrect(val) ? val : defValue;
    }

    @Nullable
    public static Double toFiniteDouble(final Double val) {
        return isDoubleCorrect(val) ? val : null;
    }
}
