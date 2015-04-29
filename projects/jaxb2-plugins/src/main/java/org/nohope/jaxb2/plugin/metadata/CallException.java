package org.nohope.jaxb2.plugin.metadata;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-30 17:31
 */
public class CallException extends Exception {
    private static final long serialVersionUID = 1L;
    private final transient IDescriptor<?> context;

    public CallException(final IDescriptor<?> context, final String message, final Throwable e) {
        super(message, e);
        this.context = context;
    }

    public IDescriptor<?> getContext() {
        return context;
    }
}
