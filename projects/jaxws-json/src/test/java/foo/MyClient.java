package foo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author Jitendra Kotamraju
 */
public class MyClient {

    public static void main(final String ... args) throws Exception {
        final URL url = new URL("http://localhost:1111/book");
        hitEndpoint(url);
    }

    static void hitEndpoint(final URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.addRequestProperty("Content-Type", "application/json");
        con.setDoInput(true);
        con.setDoOutput(true);
        // Write JSON request
        final String json = "{get:{p1:50, p2:\"test\"" +
                ", p3:\"2013-10-22T05:24:33.394Z\"" +
                ", p4: [" +
                "{f1: 1, f2: \"yyy\", f3: [{sf1: 101, sf2: \"mmm1\"}]}," +
                "{f1: 1, f2: \"yyy\", f3: [{sf1: 102, sf2: \"mmm2\"}]}" +
                "]" +
                "} }";
        final OutputStream out = con.getOutputStream();
        out.write(json.getBytes());
        out.close();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        dump(con, new PrintStream(stream));

        // dump help HTML
        //System.out.println("\n---HTML---");
        con = (HttpURLConnection) new URL(url+"?help").openConnection();

        assertEquals(200,  con.getResponseCode());
        //dump(con);
    }

    private static void dump(final HttpURLConnection con, final PrintStream out) throws IOException {
        // Check if we got the correct HTTP response code
        final int code = con.getResponseCode();

        // Check if we got the correct response
        final InputStream in = (code == 200) ? con.getInputStream() : con.getErrorStream();
        int ch;
        while((ch=in.read()) != -1) {
            out.print((char) ch);
        }
        out.println();
    }
}
