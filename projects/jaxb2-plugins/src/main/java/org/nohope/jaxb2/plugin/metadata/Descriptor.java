package org.nohope.jaxb2.plugin.metadata;

import com.sun.istack.Nullable;
import org.nohope.reflection.TypeReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 16:17
 */
public class Descriptor<T> implements IDescriptor<T> {
    private final List<IDescriptor<?>> callChain = new ArrayList<>();
    private final IDescriptor<?> parent;
    private final TypeReference<T> type;
    private final String name;

    public Descriptor(@Nullable final Descriptor<?> parent,
                      @Nullable final String name,
                      final TypeReference<T> type) {
        this.parent = parent;
        this.type = type;
        this.name = name;

        if (parent != null) {
            callChain.addAll(parent.callChain);
            callChain.add(parent);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public IDescriptor<?> getParent() {
        return parent;
    }

    @Override
    public TypeReference<T> getType() {
        return type;
    }

    @Override
    public final Iterator<IDescriptor<?>> iterator() {
        final List<IDescriptor<?>> callChain = new ArrayList<>();
        callChain.addAll(this.callChain);
        callChain.add(callChain.size(), this);

        return callChain.iterator();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final IDescriptor<?> descriptor : this) {
            if (descriptor.getParent() == null) {
                builder.append(descriptor.getType().getTypeClass().getCanonicalName());
            } else {
                builder.append('#')
                       .append(descriptor.getName())
                       .append('[')
                       .append(descriptor.getType().getTypeClass().getCanonicalName())
                       .append(']')
                       ;
            }
        }

        return builder.toString();
    }
}
