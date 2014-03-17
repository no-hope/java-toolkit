package org.nohope.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.nohope.test.ResourceUtils.getResourceAsString;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 00:17
 */
public class ResourceUtilsTest {
    @Test
    public void isUtility() throws Exception {
        UtilityClassUtils.assertUtilityClass(ResourceUtils.class);
    }

    @Test
    public void naiveTest() throws IOException {
        Assert.assertEquals(
                "this is just a test string\n",
                getResourceAsString("/resource/test.resource"));
        Assert.assertNull(getResourceAsString("/resource/nonexistent.resource"));
    }
}
