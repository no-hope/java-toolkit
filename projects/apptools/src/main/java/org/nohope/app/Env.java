package org.nohope.app;

/**
 * Date: 10/17/13
 * Time: 2:59 PM
 */
public final class Env {
    private Env() {
    }

    public static String getEnvString(final String name, final String def) {
        final String ret = System.getenv(name);
        if (null == ret) {
            return def;
        }
        return ret;
    }

    public static int getEnvInt(final String name, final Integer def) {
        final String sval = getEnvString(name, def.toString());
        return Integer.parseInt(sval);
    }
}
