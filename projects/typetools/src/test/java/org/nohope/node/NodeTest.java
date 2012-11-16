package org.nohope.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.junit.Test;
import org.nohope.typetools.node.Node;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.nohope.typetools.node.Node.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/15/12 11:21 PM
 */
public class NodeTest {
    private static final Pattern SINGLE_QUOTE = Pattern.compile("'");

    private static Node from(final BSONObject obj) {
        return new ObjectNode(obj);
    }

    @Test
    public void booleanOp() throws IOException {
        final BasicBSONObject obj0 = new BasicBSONObject();
        obj0.put("a", 1);
        obj0.put("b", 2);

        final BasicBSONObject obj1 = new BasicBSONObject();
        obj1.put("c", 3);
        obj1.put("d", 4);

        final BasicBSONObject obj2 = new BasicBSONObject();
        obj2.put("g", 5);
        obj2.put("h", 6);

        // (obj1 && !obj0) || obj2
        final Node node = or(and(from(obj1), not(from(obj0))), from(obj2));
        final String e = SINGLE_QUOTE
                .matcher("{'$or':[{'$and':[{'c':3,'d': 4},{'$not':{'a':1,'b':2}}]},{'h':6,'g':5}]}")
                .replaceAll("\"")
                ;

        final BasicBSONObject expected = new BasicBSONObject();
        expected.putAll(new ObjectMapper().readValue(e, Map.class));
        assertEquals(expected, node.evaluate(
                new MongoInterceptor() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public DBObject intercept(@Nonnull final Node node) {
                        if (node instanceof ObjectNode) {
                            final DBObject obj = new BasicDBObject();
                            obj.putAll((BSONObject) ((ObjectNode) node).getObject());
                            return obj;
                        } else {
                            return super.intercept(node);
                        }
                    }
                }));
    }
}
