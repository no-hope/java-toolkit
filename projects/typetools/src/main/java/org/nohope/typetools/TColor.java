package org.nohope.typetools;

import java.awt.*;

/**
 * Date: 11/16/12
 * Time: 3:30 PM
 */
public final class TColor {
    private TColor() {
    }

    public static String colorToHtml(final Color color) {
        String rgb = Integer.toHexString(color.getRGB());
        // remove alpha
        rgb = "#"+rgb.substring(2, rgb.length());
        return rgb;
    }
}
