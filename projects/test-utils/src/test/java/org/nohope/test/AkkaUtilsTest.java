package org.nohope.test;

import org.junit.Test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/4/12 12:27 PM
 */
public class AkkaUtilsTest {
    @Test
    public void actorSystemCreation() {
        AkkaUtils.createLocalSystem("test");
    }

}
