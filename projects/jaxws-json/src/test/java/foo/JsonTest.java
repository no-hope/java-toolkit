package foo;

import junit.framework.TestCase;
import org.nohope.test.SocketUtils;

import javax.xml.ws.Endpoint;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class JsonTest extends TestCase {
    public void test1() throws Exception {
        // publish my service
        final int port = SocketUtils.getAvailablePort();
        final String address = "http://localhost:" + port + "/book";
        Endpoint.publish(address, new MyService());
        MyClient.hitEndpoint(new URL(address));
    }
}
