package org.nohope.typetools;

import org.nohope.reflection.TypeReference;

import javax.annotation.Nullable;
import java.util.Map;

import static org.nohope.reflection.IntrospectionUtils.safeCast;

/**
 * Date: 04.08.12
 * Time: 17:35
 */
// FIXME: delete it?
public final class TMap {
    private TMap() {
    }

    public static <V> V get(final Map<String, V> map, final String key) {
        return map.get(key);
    }

    @Nullable
    public static <V, T extends V, K> T safeGet(final Map<K, V> map, final K key, final Class<T> clazz) {
        return safeCast(map.get(key), clazz);
    }

    @Nullable
    public static <V, T extends V, K> T safeGet(final Map<K, V> map, final K key, final TypeReference<T> ref) {
        return safeCast(map.get(key), ref);
    }
}
