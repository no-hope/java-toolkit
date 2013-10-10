package org.nohope.akka;

import org.nohope.test.EnumTestSupport;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-10 16:25
 */
public class AckTest extends EnumTestSupport<Ack> {
    @Override
    protected Class<Ack> getEnumClass() {
        return Ack.class;
    }
}
