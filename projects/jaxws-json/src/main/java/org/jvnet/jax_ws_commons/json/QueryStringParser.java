package org.jvnet.jax_ws_commons.json;

import com.sun.xml.ws.transport.http.WSHTTPConnection;

import java.util.HashMap;

/**
 * Quick-n-dirty query string parser.
 *
 * @author Kohsuke Kawaguchi
 */
final class QueryStringParser extends HashMap<String,String> {
    QueryStringParser(final WSHTTPConnection con) {
        this(con.getQueryString());
    }
    QueryStringParser(final String queryString) {
        if(queryString==null)   return;

        for( final String token : queryString.split("&") ) {
            final int idx = token.indexOf('=');
            if(idx<0)
                put(token,"");
            else
                put(token.substring(0,idx),token.substring(idx+1));
        }
    }
}
