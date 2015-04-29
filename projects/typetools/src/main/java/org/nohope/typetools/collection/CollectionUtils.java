package org.nohope.typetools.collection;

import org.nohope.ITranslator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/18/12 5:32 PM
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> List<T> fillList(final int count, final T placeholder) {
        final List<T> placeholders = new ArrayList<>();
        for (int i = 1; i <= count; ++i) {
            placeholders.add(placeholder);
        }
        return placeholders;
    }

    public static <K, V> Map<K, V> toMap(final Map<K, V> target,
                                         final Collection<V> collection,
                                         final ITranslator<V, K> translator) {
        for (final V value : collection) {
            target.put(translator.translate(value), value);
        }

        return target;
    }

    public static <K, V, O> Map<K, V> toExtendedMap(final Map<K, V> target,
                                                    final Collection<O> collection,
                                                    final ITranslator<O, Entry<K, V>> translator) {
        for (final O value : collection) {
            final Entry<K, V> entry = translator.translate(value);
            target.put(entry.getKey(), entry.getValue());
        }

        return target;
    }

    public static <K, V, C extends Collection<K>> C
            toCollection(final C target,
                         final Collection<V> collection,
                         final ITranslator<V, K> translator) {
        target.addAll(collection.stream().map(translator::translate).collect(Collectors.toList()));
        return target;
    }

    public static <K, V, C extends Collection<K>> C
            toCollection(final C target,
                         final V[] collection,
                         final ITranslator<V, K> translator) {
        for (final V value : collection) {
            target.add(translator.translate(value));
        }

        return target;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> K[] mapArray(final V[] collection,
                                      final Class<K> targetClazz,
                                      final ITranslator<V, K> translator) {
        final Collection<K> ks = toCollection(new ArrayList<K>(), collection, translator);
        return ks.toArray((K[]) Array.newInstance(targetClazz, ks.size()));
    }
}
