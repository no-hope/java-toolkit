package org.nohope.node;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.nohope.typetools.node.Node;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/15/12 5:49 PM
 */
public class ObjectNode extends Node {
    private static final long serialVersionUID = 1L;
    private final BasicBSONObject obj;

    public ObjectNode(final BSONObject obj) {
        this.obj = new BasicBSONObject();
        this.obj.putAll(obj);
    }

    public BasicBSONObject getObject() {
        return obj;
    }
}
