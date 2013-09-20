package org.nohope.typetools.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/16/12 3:39 AM
 */
public abstract class ListNode extends Node {
    private static final long serialVersionUID = 1L;

    private final List<Node> children = new ArrayList<>();

    public ListNode(final Node... nodes) {
        children.addAll(Arrays.asList(nodes));
    }

    public<R> List<R> getChildValues(final Interceptor<? extends Node, R> interceptor) {
        final List<R> result = new ArrayList<>();
        for (final Node child : children) {
            result.add(child.evaluate(interceptor));
        }

        return result;
    }
}
