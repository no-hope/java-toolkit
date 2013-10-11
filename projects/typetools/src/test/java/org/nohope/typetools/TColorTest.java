package org.nohope.typetools;

import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;

import java.awt.*;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 12:27
 */
public class TColorTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return TColor.class;
    }

    @Test
    public void htmlCode() {
        assertEquals("#000000", TColor.colorToHtml(Color.BLACK));
        assertEquals("#ff0000", TColor.colorToHtml(Color.RED));
        assertEquals("#00ff00", TColor.colorToHtml(Color.GREEN));
        assertEquals("#0000ff", TColor.colorToHtml(Color.BLUE));
        assertEquals("#ffffff", TColor.colorToHtml(Color.WHITE));

        assertEquals("#808080", TColor.colorToHtml(Color.GRAY));
    }
}
