package org.nohope.typetools;

import org.junit.Test;
import org.nohope.test.TRandom;
import org.nohope.test.UtilitiesTestSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-10 02:30
 */
public class TNumbTest extends UtilitiesTestSupport<TNumb> {

    @Override
    protected Class<TNumb> getUtilityClass() {
        return TNumb.class;
    }

    @Test
    public void parseInt() {
         assertNull(TNumb.parseInt(TRandom.standard().nextString(10, "!aA")));
         assertEquals((Object) 123, TNumb.parseInt(TRandom.standard().nextString(10, "!aA"), 123));
         assertEquals((Object) 123, TNumb.parseInt("0123"));
         assertNull(TNumb.parseInt(null, null));
    }
}
