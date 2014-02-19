package org.jvnet.jax_ws_commons.json;

import com.sun.istack.NotNull;
import com.sun.istack.XMLStreamException2;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.JAXPParser;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.jvnet.jax_ws_commons.json.schema.CompositeJsonType;
import org.jvnet.jax_ws_commons.json.schema.JsonOperation;
import org.jvnet.jax_ws_commons.json.schema.JsonTypeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Captures the information parsed from XML Schema.
 * Used to guide the JSON/XML conversion.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SchemaInfo {
    /**
     * Endpoint for which this schema info applies.
     */
    @NotNull private final WSEndpoint endpoint;

    /**
     * Parent tag name to possible child tag names.
     */
    private final Set<QName> tagNames = new HashSet<>();

    final List<JsonOperation> operations = new ArrayList<>();

    final SchemaConvention convention;
    private static final SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();


    /**
     * @throws WebServiceException If failed to parse schema portion inside WSDL.
     */
    public SchemaInfo(final WSEndpoint endpoint) {
        this.endpoint = endpoint;

        final ServiceDefinition sd = endpoint.getServiceDefinition();
        final Map<String, SDDocument> byURL = new HashMap<>();

        for (final SDDocument doc : sd) {
            byURL.put(doc.getURL().toExternalForm(), doc);
        }

        // set up XSOMParser to read from SDDocuments
        final XSOMParser p = new XSOMParser(new XMLParser() {
            private final XMLParser jaxp = new JAXPParser();

            @Override
            public void parse(final InputSource source, final ContentHandler handler, final ErrorHandler errorHandler, final EntityResolver entityResolver) throws SAXException, IOException {
                final SDDocument doc = byURL.get(source.getSystemId());
                if (doc != null) {
                    try {
                        readToBuffer(doc).writeTo(handler, errorHandler, false);
                    } catch (final XMLStreamException e) {
                        throw new SAXException(e);
                    }
                } else {
                    // default behavior
                    jaxp.parse(source, handler, errorHandler, entityResolver);
                }
            }
        });

        try {
            // parse the primary WSDL, and it should recursively parse all referenced schemas

            // TODO: this is super slow
            final TransformerHandler h = saxTransformerFactory.newTransformerHandler();

            final DOMResult r = new DOMResult();
            h.setResult(r);
            readToBuffer(sd.getPrimary()).writeTo(h, false);
            final Document dom = (Document) r.getNode();
            final NodeList schemas = dom.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
            for (int i = 0; i < schemas.getLength(); i++) {
                final DOMScanner scanner = new DOMScanner();
                scanner.setContentHandler(p.getParserHandler());
                scanner.scan(schemas.item(i));
            }

            extractTagNames(p.getResult());
            convention = new SchemaConvention(tagNames);

            if (endpoint.getPort() != null) {
                buildJsonSchema(p.getResult(), endpoint.getPort());
            }
        } catch (XMLStreamException | SAXException | IOException e) {
            throw new WebServiceException("Failed to parse WSDL", e);
        } catch (final TransformerConfigurationException e) {
            throw new AssertionError(e); // impossible
        }
    }

    public String getServiceName() {
        String name = endpoint.getPort().getName().getLocalPart();
        if (name.endsWith("ServicePort"))
        // when doing java2wsdl and the class name ends with 'Service', you get this.
        {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    public XMLStreamWriter createXMLStreamWriter(final Writer writer) {
        return new MappedXMLStreamWriter(convention, writer) {
            @Override
            public void writeEndDocument() throws XMLStreamException {
                if (!stack.isEmpty()) {
                    throw new XMLStreamException("Missing some closing tags.");
                }
                try {
                    // We know the root is a JSONPropertyObject so this cast is safe
                    JSONObject root = (JSONObject) current.getValue();

                    // the root is the "XXXresponse" object, remove it since it is of
                    // no interest
                    root = root.getJSONObject((String) root.keys().next());

                    //final Object v = root;
                    // if this is the sole return value unwrap that, too
                /*if (root.length() == 1)
                        v = root.get((String) root.keys().next());
        	    else
        		v = root;*/

                    if (root == null) {
                        writer.write("null");
                    } else {
                        final Object firstElement = root.get((String) root.keys().next());
                        if (root.length() == 1) {
                            if (firstElement == null) {
                                writer.write("null");
                            } else if (firstElement instanceof JSONArray) {
                                ((JSONArray) firstElement).write(writer);
                            } else {
                                root.write(writer);
                            }
                        } else {
                            root.write(writer);
                        }
                    }


                    writer.flush();
                } catch (JSONException | IOException e) {
                    throw new XMLStreamException2(e);
                }
            }
        };
    }

    public XMLStreamReader createXMLStreamReader(final JSONTokener tokener) throws JSONException, XMLStreamException {
        final JSONObject obj = new JSONObject(tokener);

        /*
        final XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        final ByteArrayOutputStream byte1=new ByteArrayOutputStream();
        final XMLStreamWriter xmlw = new IndentingXMLStreamWriter(xmlof.createXMLStreamWriter(byte1));
        final StAXResult result = new StAXResult(xmlw);
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer t;
        try {
            t = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        final StAXSource source = new StAXSource(new MappedXMLStreamReader(obj, convention));

        try {
            t.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        xmlw.close();
        System.err.println("OBJ: " + obj);
        System.err.println("CON: "+ JSON.JSON.pretty(convention));
        System.err.println("RST: "+byte1.toString());
        */

        return new MappedXMLStreamReader(obj, convention);
    }

    /**
     * Extracts parent/child tag name relationship.
     */
    private void extractTagNames(final XSSchemaSet schemas) {
        final XSVisitor collector = new SchemaWalker() {
            @Override
            public void elementDecl(final XSElementDecl decl) {
                tagNames.add(new QName(decl.getTargetNamespace(), decl.getName()));
            }
        };
        for (final XSSchema s : schemas.getSchemas()) {
            s.visit(collector);
        }
    }

    private static MutableXMLStreamBuffer readToBuffer(final SDDocument doc) throws XMLStreamException, IOException {
        final MutableXMLStreamBuffer buf = new MutableXMLStreamBuffer();
        doc.writeTo(null, resolver, new StreamWriterBufferCreator(buf));
        return buf;
    }

    private static final DocumentAddressResolver resolver = new DocumentAddressResolver() {
        @Override
        public String getRelativeAddressFor(@NotNull final SDDocument current, @NotNull final SDDocument referenced) {
            return referenced.getURL().toExternalForm();
        }
    };

    private void buildJsonSchema(final XSSchemaSet schemas, final WSDLPort port) {
        final Style style = port.getBinding().getStyle();
        final JsonTypeBuilder builder = new JsonTypeBuilder(convention);
        for (final WSDLBoundOperation bo : port.getBinding().getBindingOperations()) {
            operations.add(new JsonOperation(bo, schemas, builder, style));
        }
    }

    public List<JsonOperation> getOperations() {
        return operations;
    }

    public Set<CompositeJsonType> getTypes() {
        final Set<CompositeJsonType> r = new LinkedHashSet<>();
        for (final JsonOperation op : operations) {
            op.input.listCompositeTypes(r);
            op.output.listCompositeTypes(r);
        }
        return r;
    }

    //private static final String WSDL_NSURI = "http://schemas.xmlsoap.org/wsdl/";
}
