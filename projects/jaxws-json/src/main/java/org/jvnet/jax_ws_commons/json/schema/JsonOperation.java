package org.jvnet.jax_ws_commons.json.schema;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPart;
import com.sun.xml.ws.api.model.wsdl.WSDLPartDescriptor;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchemaSet;

import javax.jws.soap.SOAPBinding.Style;
import java.beans.Introspector;
import java.util.Map;

/**
 * Represents the JSON type signature of an operation.
 *
 * @author Kohsuke Kawaguchi
 */
public class JsonOperation {
    /**
     * Method name of this operation, inferred from the operation name.
     */
    public final String methodName;
    public final JsonType input, output;
    /**
     * This JSON operation is modeled after this WSDL operation.
     */
    public final WSDLBoundOperation operation;

    public JsonOperation(final WSDLBoundOperation bo, final XSSchemaSet schemas, final JsonTypeBuilder builder, final Style style) {
        operation = bo;
        methodName = Introspector.decapitalize(bo.getName().getLocalPart());

        input = build(operation.getOperation().getInput().getName(), schemas, bo.getInParts(), builder, style);
        // if the return type has only one property we also unwrap that.
        // see SchemaInfo#createXMLStreamWriter
        output = build(operation.getOperation().getOutput().getName(), schemas, bo.getOutParts(), builder, style).unwrap();
    }

    /**
     * Infer the JavaScript type from the given parts set.
     */
    private static JsonType build(final String name,
                                  final XSSchemaSet schemas,
                                  final Map<String, WSDLPart> parts,
                                  final JsonTypeBuilder builder,
                                  final Style style) {
        final CompositeJsonType wrapper = new CompositeJsonType(name);
        for (final Map.Entry<String, WSDLPart> in : parts.entrySet()) {
            if (!in.getValue().getBinding().isBody()) {
                continue;   // JSON binding has no header support for now.
            }
            final WSDLPartDescriptor d = in.getValue().getDescriptor();

            switch (d.type()) {
                case ELEMENT:
                    final XSElementDecl decl = schemas.getElementDecl(d.name().getNamespaceURI(), d.name().getLocalPart());
                    wrapper.properties.put(in.getKey(), builder.create(decl.getType()));
                    break;
                case TYPE:
                    wrapper.properties.put(in.getKey(), builder.create(
                            schemas.getType(d.name().getNamespaceURI(), d.name().getLocalPart())));
                    break;
            }
        }

        if (style == Style.DOCUMENT)
        // peel off the outermost part that doesn't actually have a representation on the wire.
        {
            return wrapper.unwrap();
        } else {
            return wrapper;
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public JsonType getInput() {
        return input;
    }

    public JsonType getOutput() {
        return output;
    }
}
