package org.nohope.protobuf.core;

import org.junit.Test;
import org.nohope.rpc.protocol.RPC;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 13:39
 */
public class ChannelTest {
    @Test
    public void channelProperties() {
        final Controller controller = new Controller();
        assertFalse(controller.failed());
        assertFalse(controller.isCanceled());
        assertNull(controller.errorText());
        assertNull(controller.getError());

        final RPC.Error error =
                RPC.Error
                   .newBuilder()
                   .setErrorCode(RPC.ErrorCode.RPC_ERROR)
                   .setErrorMessage("test")
                   .build()
                   ;

        controller.setError(error);
        assertEquals("test", controller.errorText());
        assertEquals(error, controller.getError());
        assertTrue(controller.failed());
        assertFalse(controller.isCanceled());

        controller.reset();
        assertFalse(controller.failed());
        assertFalse(controller.isCanceled());
        assertNull(controller.errorText());
        assertNull(controller.getError());

        controller.setFailed("test2");
        assertEquals("test2", controller.errorText());
        assertNull(controller.getError());

        controller.reset();
        controller.startCancel();
        assertTrue(controller.isCanceled());

        controller.reset();
        controller.notifyOnCancel(null);
    }
}
