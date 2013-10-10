package org.nohope.test;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.nohope.test.ResourceUtils.getResourceAsString;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 00:17
 */
public class ResourceUtilsTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return ResourceUtils.class;
    }

    @Test
    public void naiveTest() throws IOException {
        assertEquals(
                "this is just a test string\n",
                getResourceAsString("/resource/test.resource"));
        assertNull(getResourceAsString("/resource/nonexistent.resource"));
    }
}
