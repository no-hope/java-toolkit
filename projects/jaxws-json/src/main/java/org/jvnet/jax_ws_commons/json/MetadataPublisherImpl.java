package org.jvnet.jax_ws_commons.json;

import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Responds to "http://foobar/service?res" and sends the JavaScript proxy.
 *
 * @author Kohsuke Kawaguchi
 */
final class MetadataPublisherImpl extends HttpMetadataPublisher {
    private final SchemaInfo model;

    public MetadataPublisherImpl(final SchemaInfo model) {
        this.model = model;
    }

    @Override
    public boolean handleMetadataRequest(@NotNull final HttpAdapter adapter, @NotNull final WSHTTPConnection con) throws IOException {
        final QueryStringParser qsp = new QueryStringParser(con);
        if(qsp.containsKey("js")) {
            // JavaScript proxy code
            con.setStatus(HttpURLConnection.HTTP_OK);
            con.setContentTypeResponseHeader("application/javascript;charset=utf-8");

            final ClientGenerator gen = new ClientGenerator(model, con, adapter);
            final String varName = qsp.get("var");
            if(varName!=null)
                gen.setVariableName(varName);

            gen.generate(new PrintWriter(
                new OutputStreamWriter(con.getOutput(),"UTF-8")));
            return true;
        }

        if(con.getQueryString()==null || qsp.containsKey("help")) {
            // index page
            con.setStatus(HttpURLConnection.HTTP_OK);
            con.setContentTypeResponseHeader("text/html;charset=UTF-8");

            generateHelpHtml(con,adapter,new OutputStreamWriter(con.getOutput(), "UTF-8"));
            return true;
        }

        final URL res = getClass().getResource("template/" + con.getQueryString());
        if(res!=null) {
            // static resource accesss
            con.setStatus(HttpURLConnection.HTTP_OK);
            if(res.getPath().endsWith(".gif"))
                con.setContentTypeResponseHeader("image/gif");
            if(res.getPath().endsWith(".css"))
                con.setContentTypeResponseHeader("text/css");

            final InputStream is = res.openStream();
            final OutputStream os = con.getOutput();
            final byte[] buf = new byte[1024];
            int len;
            while((len=is.read(buf))>=0)
                os.write(buf,0,len);
            is.close();
            os.close();
            return true;
        }

        return false;
    }

    /*package for testing*/ void generateHelpHtml(final WSHTTPConnection con, final HttpAdapter adapter, final OutputStreamWriter writer) throws IOException {
        final VelocityContext context = new VelocityContext();
        context.put("model",model);
        context.put("requestURL",con.getBaseAddress()+adapter.urlPattern);

        new VelocityEngine().evaluate(context, writer, "velocity",
            new InputStreamReader(getClass().getResourceAsStream("template/index.html"),"UTF-8")
            );
        writer.close();
    }
}
