package org.nohope.akka.invoke;

import java.util.Arrays;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
* @since 2014-04-11 19:36
*/
final class Signature {
    private final ComparatorProvider provider;
    private final Class<?>[] handlers;
    private final Class<?>[] parameter;

    Signature(final ComparatorProvider provider,
              final Class<?>[] parameter,
              final Class<?>... handlers) {
        this.provider = provider;
        this.handlers = handlers;
        this.parameter = parameter == null ? null : parameter.clone();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Signature signature = (Signature) o;
        return Arrays.equals(handlers, signature.handlers)
            && Arrays.equals(parameter, signature.parameter)
            && provider.equals(signature.provider);
    }

    @Override
    public int hashCode() {
        int result = provider.hashCode();
        result = 31 * result + Arrays.hashCode(handlers);
        result = 31 * result + Arrays.hashCode(parameter);
        return result;
    }

    public static Signature of(final ComparatorProvider provider,
                               final Class<?>[] parameter,
                               final Class<?>... handlers) {
        return new Signature(provider, parameter, handlers);
    }
}
