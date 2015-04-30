package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import org.jvnet.jax_ws_commons.json.schema.JsonOperation;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Iterator;

/**
 * Generates javascript stub code that is used to access the endpoint.
 *
 * @author Jitendra Kotamraju
 */
final class ClientGenerator {
    private final SchemaInfo model;
    private final WSHTTPConnection connection;
    private final HttpAdapter adapter;
    private String name;

    public ClientGenerator(final SchemaInfo model, final WSHTTPConnection connection, final HttpAdapter adapter) {
        this.model = model;
        this.connection = connection;
        this.adapter = adapter;
        this.name = Introspector.decapitalize(model.getServiceName());
    }

    public void setVariableName(final String name) {
        this.name = name;
    }

    void generate(final PrintWriter os) throws IOException {
        writeGlobal(os);
        writeStatic(os);
        writeOperations(os);
        writeClosure(os);
        os.close();
    }

    private void writeGlobal(final PrintWriter os) {
        os.printf("%s = {\n", name);
        shift(os);
        os.printf("url : \"%s\",\n", connection.getBaseAddress() + adapter.urlPattern);
    }

    private void writeStatic(final PrintWriter os) throws IOException {
        final Reader is = new InputStreamReader(getClass().getResourceAsStream("jaxws.js"));
        final char[] buf = new char[256];
        int len;
        while ((len = is.read(buf)) != -1) {
            os.write(buf, 0, len);
        }
        is.close();
    }

    private void writeOperations(final PrintWriter os) {
        final Iterator<JsonOperation> it = model.operations.iterator();
        while (it.hasNext()) {
            writeOperation(it.next(), it.hasNext(), os);
        }
    }

    private void writeOperation(final JsonOperation op, final boolean next, final PrintWriter os) {
        final String reqName = model.convention.x2j.get(op.operation.getRequestPayloadName());
        shift(os);
        os.printf("%s : function(obj, callback) {\n", op.methodName);
        shift2(os);
        os.printf("this.post({%s:obj},callback);\n", reqName);
        shift(os);
        if (next) {
            os.append("},\n\n");
        } else {
            os.append("}\n\n");
        }
    }

    private static void shift(final PrintWriter os) {
        os.append("    ");
    }

    private static void shift2(final PrintWriter os) {
        shift(os);
        shift(os);
    }

    private static void writeClosure(final PrintWriter os) {
        os.println("};");
    }
}
