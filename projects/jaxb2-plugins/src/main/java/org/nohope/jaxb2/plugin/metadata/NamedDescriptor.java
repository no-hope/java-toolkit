package org.nohope.jaxb2.plugin.metadata;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/30/13 3:13 PM
 */
public class NamedDescriptor<T> {
    private final IDescriptor<T> parentDescriptor;
    private final String property;

    public NamedDescriptor(final IDescriptor<T> descriptor, final String property) {
        this.parentDescriptor = descriptor;
        this.property = property;
    }

    public IDescriptor<T> getParentDescriptor() {
        return parentDescriptor;
    }

    public String getProperty() {
        return property;
    }
}
