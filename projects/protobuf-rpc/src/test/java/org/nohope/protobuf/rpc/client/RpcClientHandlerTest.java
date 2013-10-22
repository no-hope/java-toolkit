package org.nohope.protobuf.rpc.client;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.nohope.protobuf.rpc.client.RpcChannelImpl.ResponsePrototypeRpcCallback;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-23 01:06
 */
public class RpcClientHandlerTest {
    @Test
    public void register() {
        final RpcClientHandler handler = new RpcClientHandler();
        final ResponsePrototypeRpcCallback mock = createMock(ResponsePrototypeRpcCallback.class);

        handler.registerCallback(1, mock);
        try {
            handler.registerCallback(1, mock);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void exceptionCaught() throws Exception {
        final ChannelHandlerContext ctx = createMock(ChannelHandlerContext.class);
        final Channel c = createMock(Channel.class);
        final Throwable cause = new Throwable();
        final ExceptionEvent ee = new DefaultExceptionEvent(c, cause);
        final RpcClientHandler handler = new RpcClientHandler();
        try {
            handler.exceptionCaught(ctx, ee);
        } catch (Exception e) {
            assertSame(cause, e.getCause());
        }
    }
}
