package org.nohope.jaxb2.plugin.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 3:03 PM
 */
public final class CallChain {
    private final List<NamedDescriptor<?>> chain = new ArrayList<>();

    public <T> CallChain add(final IDescriptor<T> descriptor, final String property) {
        final CallChain copy = new CallChain();
        copy.chain.addAll(this.chain);
        copy.chain.add(new NamedDescriptor<>(descriptor, property));
        return copy;
    }

    public List<NamedDescriptor<?>> getChain() {
        return new ArrayList<>(chain);
    }
}
