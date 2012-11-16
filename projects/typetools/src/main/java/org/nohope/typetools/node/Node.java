package org.nohope.typetools.node;

import java.io.Serializable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/15/12 5:35 PM
 */
public abstract class Node implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public final <N extends Node, R> R evaluate(final Interceptor<N, R> interceptor) {
        return interceptor.intercept((N) this);
    }

    public static Node and(final Node... nodes) {
        return new AndNode(nodes);
    }

    public static Node or(final Node... nodes) {
        return new OrNode(nodes);
    }

    public static Node not(final Node node) {
        return new NotNode(node);
    }

    public static Node empty() {
        return new EmptyNode();
    }
}
