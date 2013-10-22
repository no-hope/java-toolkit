package org.nohope.protobuf.rpc.client;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.easymock.Capture;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static java.util.Map.Entry;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-23 00:10
 */
public class RpcChannelImplTest {
    private static Entry<Channel, ChannelPipeline> mockChannel(
            final Capture<Class<? extends ChannelHandler>> type,
            final RpcClientHandler handler) {
        final Channel channel = createMock(Channel.class);
        final ChannelPipeline pipeline = createMock(ChannelPipeline.class);
        expect(channel.getPipeline()).andReturn(pipeline);
        expect(pipeline.get(capture(type))).andReturn(handler);
        return new ImmutablePair<>(channel, pipeline);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void nonBlockingCall() {
        final Capture<Class<? extends ChannelHandler>> type = new Capture<>();
        final Entry<Channel, ChannelPipeline> pair =
                mockChannel(type, new RpcClientHandler());
        final Channel channel = pair.getKey();
        final ChannelPipeline pipeline = pair.getValue();
        final RpcController controller = createMock(RpcController.class);
        replay(channel, pipeline, controller);
        new RpcChannelImpl(channel, 0, TimeUnit.SECONDS).callMethod(null, null, null, null, null);
        // TODO...
        verify(controller, pipeline, controller);
    }

    @Test
    public void illegalArguments() {
        {
            final Capture<Class<? extends ChannelHandler>> type = new Capture<>();
            final Entry<Channel, ChannelPipeline> pair = mockChannel(type, null);
            final Channel channel = pair.getKey();
            final ChannelPipeline pipeline = pair.getValue();
            replay(channel, pipeline);

            try {
                new RpcChannelImpl(channel, 0, TimeUnit.SECONDS);
                fail();
            } catch (final IllegalArgumentException e) {
            }

            verify(channel, pipeline);
            assertEquals(RpcClientHandler.class, type.getValue());
        }
        {
            final Capture<Class<? extends ChannelHandler>> type = new Capture<>();
            final Entry<Channel, ChannelPipeline> pair =
                    mockChannel(type, new RpcClientHandler());
            final Channel channel = pair.getKey();
            final ChannelPipeline pipeline = pair.getValue();
            final RpcController controller = createMock(RpcController.class);
            replay(channel, pipeline, controller);

            try {
                new RpcChannelImpl(channel, 0, TimeUnit.SECONDS)
                        .callBlockingMethod(null, controller, null, null);
                fail();
            } catch (ServiceException e) {
                fail();
            } catch (final IllegalArgumentException ignored) {
            }

            verify(controller, pipeline, controller);
        }
    }
}
