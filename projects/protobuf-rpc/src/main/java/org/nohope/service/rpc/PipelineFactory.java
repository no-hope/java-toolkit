package org.nohope.service.rpc;

import org.nohope.protocol.RPC;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/19/13 6:16 PM
 */
public class PipelineFactory implements ChannelPipelineFactory {
    private static final int MAX_FRAME_BYTES_LENGTH = Integer.MAX_VALUE;
    private final ChannelUpstreamHandler handler;

    public PipelineFactory(final ChannelUpstreamHandler handler) {
        this.handler = handler;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        final ChannelPipeline p = Channels.pipeline();
        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_BYTES_LENGTH, 0, 4, 0, 4));
        p.addLast("protobufDecoder", new ProtobufDecoder(RPC.RpcRequest.getDefaultInstance()));

        p.addLast("frameEncoder", new LengthFieldPrepender(4));
        p.addLast("protobufEncoder", new ProtobufEncoder());
        p.addLast("handler", handler);
        return p;
    }
}
