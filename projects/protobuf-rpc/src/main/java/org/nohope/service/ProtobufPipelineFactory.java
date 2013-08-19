package org.nohope.service;

import com.google.protobuf.MessageLite;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/19/13 6:16 PM
 */
public class ProtobufPipelineFactory implements ChannelPipelineFactory {
    private static final int MAX_FRAME_BYTES_LENGTH = Integer.MAX_VALUE;

    private final List<MessageLite> defaultInstances = new ArrayList<>();
    private final ChannelUpstreamHandler handler;

    public ProtobufPipelineFactory(final ChannelUpstreamHandler handler, final MessageLite... defaultInstances) {
        this.handler = handler;
        this.defaultInstances.addAll(Arrays.asList(defaultInstances));
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        final ChannelPipeline p = Channels.pipeline();
        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_BYTES_LENGTH, 0, 4, 0, 4));
        for (final MessageLite instance : this.defaultInstances) {
            p.addLast("protobufDecoder-" + instance.getClass(), new ProtobufDecoder(instance));
        }

        p.addLast("frameEncoder", new LengthFieldPrepender(4));
        p.addLast("protobufEncoder", new ProtobufEncoder());
        p.addLast("handler", handler);
        return p;
    }
}
