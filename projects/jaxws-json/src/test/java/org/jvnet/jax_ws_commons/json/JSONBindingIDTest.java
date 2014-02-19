package org.jvnet.jax_ws_commons.json;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-02-19 12:49
 */
public class JSONBindingIDTest {

    @Test
    public void defaults() {
        final JSONBindingID id = new JSONBindingID();
        assertTrue(id.canGenerateWSDL());
        assertEquals(id.getSOAPVersion().contentType, "text/xml");
    }
}
