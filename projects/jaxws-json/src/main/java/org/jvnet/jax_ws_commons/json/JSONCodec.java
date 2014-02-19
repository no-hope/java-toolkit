package org.jvnet.jax_ws_commons.json;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.server.EndpointAwareCodec;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONTokener;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;


/**
 * Server-side {@link Codec} that generates JSON.
 *
 * @author Jitendra Kotamraju
 */
class JSONCodec implements EndpointAwareCodec, EndpointComponent {

    private static final String JSON_MIME_TYPE = "application/json";
    private static final ContentType jsonContentType = new JSONContentType();

    private final WSBinding binding;
    private final SOAPVersion soapVersion;

    private SchemaInfo schemaInfo;
    private HttpMetadataPublisher metadataPublisher;
    private WSEndpoint endpoint;

    public JSONCodec(final WSBinding binding) {
        this.binding = binding;
        this.soapVersion = binding.getSOAPVersion();
    }

    public JSONCodec(final JSONCodec that) {
        this(that.binding);
        this.schemaInfo = that.schemaInfo;
        this.endpoint = that.endpoint;
    }

    @Override
    public void setEndpoint(final WSEndpoint endpoint) {
        this.endpoint = endpoint;
        schemaInfo = new SchemaInfo(endpoint);
        endpoint.getComponentRegistry().add(this);
    }

    @Override
    public String getMimeType() {
        return JSON_MIME_TYPE;
    }

    @Override
    public ContentType getStaticContentType(final Packet packet) {
        return jsonContentType;
    }


    @Nullable
    @Override
    public <T> T getSPI(@NotNull final Class<T> type) {
        if (type == HttpMetadataPublisher.class) {
            if (metadataPublisher == null) {
                metadataPublisher = new MetadataPublisherImpl(checkSchemaInfo());
            }
            return type.cast(metadataPublisher);
        }
        return null;
    }

    @Override
    public ContentType encode(final Packet packet, final OutputStream out) throws IOException {
        final Message message = packet.getMessage();
        if (message != null) {
            XMLStreamWriter sw = null;
            try {
                sw = checkSchemaInfo().createXMLStreamWriter(new OutputStreamWriter(out, "UTF-8"));
                sw.writeStartDocument();
                message.writePayloadTo(sw);
                sw.writeEndDocument();
            } catch (final XMLStreamException xe) {
                throw new WebServiceException(xe);
            } finally {
                if (sw != null) {
                    try {
                        sw.close();
                    } catch (final XMLStreamException xe) {
                        // let the original exception get through
                    }
                }
            }
        }
        return jsonContentType;
    }

    @Override
    public ContentType encode(final Packet packet, final WritableByteChannel buffer) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the up-to-date {@link SchemaInfo} for the current endpoint,
     * by either using a cache or by parsing new.
     */
    private SchemaInfo checkSchemaInfo() {
        if (schemaInfo == null) {
            throw new IllegalStateException("JSON binding is only available for the server");
        }
        return schemaInfo;
    }

    @Override
    public Codec copy() {
        return new JSONCodec(this);
    }

    @Override
    public void decode(final InputStream in, final String contentType, final Packet response) throws IOException {
        final Message message;
        try {
            final StringWriter sw = new StringWriter();
            // TODO: RFC-4627 calls for BOM check
            // TODO: honor charset sub header.
            final Reader r = new InputStreamReader(in, "UTF-8");
            final char[] buf = new char[1024];
            int len;
            while ((len = r.read(buf)) >= 0) {
                sw.write(buf, 0, len);
            }
            r.close();

            if (sw.getBuffer().length() == 0) {
                // no content
                message = Messages.createEmpty(soapVersion);
            } else {
                final String incomingPacket = sw.toString();
                final XMLStreamReader reader = checkSchemaInfo().createXMLStreamReader(new JSONTokener(incomingPacket));
                //com.sun.xml.ws.message.stream.PayloadStreamReaderMessage

                message = Messages.createUsingPayload(reader, soapVersion);
                //System.err.println(">>>>>>>>>>>>>>>>>>> 2: "+ JSON.JSON.pretty(message));
            }
        } catch (final XMLStreamException | JSONException e) {
            throw new WebServiceException(e);
        }

        response.setMessage(message);
    }

    @Override
    public void decode(final ReadableByteChannel in, final String contentType, final Packet response) {
        throw new UnsupportedOperationException();
    }

    private static final class JSONContentType implements ContentType {

        private static final String JSON_CONTENT_TYPE = JSON_MIME_TYPE;

        @Override
        public String getContentType() {
            return JSON_CONTENT_TYPE;
        }

        @Override
        public String getSOAPActionHeader() {
            return null;
        }

        @Override
        public String getAcceptHeader() {
            return JSON_CONTENT_TYPE;
        }

    }
}
