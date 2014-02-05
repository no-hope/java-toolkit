package org.nohope.protobuf.rpc.client;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 12:39
 */
public class RpcClientOptionsTest {
    @Test
    public void builder() {
        final InetSocketAddress address = new InetSocketAddress("localhost", 1234);
        final long timeout = 10;
        final TimeUnit unit = TimeUnit.DAYS;
        final RpcClientOptions options =
                new RpcClientOptions.Builder(address)
                        .setTimeout(timeout)
                        .setTimeoutUnit(unit)
                        .build();

        assertEquals(address, options.getAddress());
        assertEquals(timeout, options.getTimeout());
        assertEquals(unit, options.getTimeoutUnit());
    }
}
