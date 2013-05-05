package org.nohope.typetools;

import javax.annotation.Nullable;
import java.lang.NumberFormatException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 5/5/13 9:16 PM
 */
public class TNumb {
    private TNumb() {
    }

    @Nullable
    public static Integer parseInt(@Nullable final String text) {
        return parseInt(text, null);
    }

    @Nullable
    public static Integer parseInt(@Nullable final String text, @Nullable final Integer def) {
        if (text == null) {
            return null;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
