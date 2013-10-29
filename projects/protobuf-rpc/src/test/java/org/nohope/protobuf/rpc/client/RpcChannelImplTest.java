package org.nohope.protobuf.rpc.client;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.easymock.Capture;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.junit.Test;
import org.nohope.protobuf.core.net.PipelineFactory;
import org.nohope.rpc.protocol.RPC;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
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
        final Entry<Channel, ChannelPipeline> pair = mockChannel(type, new RpcClientHandler());
        final Channel channel = pair.getKey();
        final ChannelPipeline pipeline = pair.getValue();
        final RpcController controller = createMock(RpcController.class);
        final ChannelFactory factory = createMock(ChannelFactory.class);
        replay(channel, pipeline, controller, factory);

        final ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootstrap.setOption("remoteAddress", new InetSocketAddress("localhost", 1111));
        bootstrap.setPipelineFactory(new PipelineFactory(new RpcClientHandler(),
                RPC.RpcResponse.getDefaultInstance()));

        new RpcChannelImpl(bootstrap, 0, TimeUnit.SECONDS).callMethod(null, null, null, null, null);
        // TODO...
        verify(controller, pipeline, controller);
    }

    @Test
    public void illegalArguments() throws Exception {
        {
            final Capture<Class<? extends ChannelHandler>> type = new Capture<>();
            final Entry<Channel, ChannelPipeline> pair = mockChannel(type, null);
            final Channel channel = pair.getKey();
            final ChannelPipeline pipeline = pair.getValue();
            final ClientBootstrap bootstrap = createMock(ClientBootstrap.class);
            final ChannelFuture future = createMock(ChannelFuture.class);
            expect(future.getChannel()).andReturn(channel);
            expect(bootstrap.connect()).andReturn(future);

            replay(channel, pipeline, future, bootstrap);

            try {
                new RpcChannelImpl(bootstrap, 0, TimeUnit.SECONDS);
                fail();
            } catch (final IllegalArgumentException e) {
            }

            verify(channel, pipeline, future, bootstrap);
            assertEquals(RpcClientHandler.class, type.getValue());
        }
        {
            final Capture<Class<? extends ChannelHandler>> type = new Capture<>();
            final Entry<Channel, ChannelPipeline> pair =
                    mockChannel(type, new RpcClientHandler());
            final Channel channel = pair.getKey();
            final ChannelPipeline pipeline = pair.getValue();
            final ClientBootstrap bootstrap = createMock(ClientBootstrap.class);
            final ChannelFuture future = createMock(ChannelFuture.class);
            expect(future.getChannel()).andReturn(channel);
            expect(bootstrap.connect()).andReturn(future);

            final RpcController controller = createMock(RpcController.class);
            replay(channel, pipeline, controller, future, bootstrap);

            try {
                new RpcChannelImpl(bootstrap, 0, TimeUnit.SECONDS)
                        .callBlockingMethod(null, controller, null, null);
                fail();
            } catch (ServiceException e) {
               fail();
            } catch (final IllegalArgumentException ignored) {
            }

            verify(channel, pipeline, controller, future, bootstrap);
        }
    }
}
