package org.nohope.protobuf.core.net;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.defaultThreadFactory;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.nohope.protobuf.core.MessageUtils.getExtensionRegistry;

/**
 * Set of decoders/encoders to convert packet format from/to
 * {@link org.nohope.rpc.protocol.RPC.RpcRequest RpcRequest} or
 * {@link org.nohope.rpc.protocol.RPC.RpcResponse RpcResponse}.
 * <p/>
 * <b>Packet format</b>:
 * <pre>
 * +----------------------+----------------+
 * | Serialized RpcObject |   Serialized   |
 * |   Length (4 bytes)   | RpcObject body |
 * +----------------------+----------------+
 * </pre>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/19/13 6:16 PM
 */
public final class PipelineFactory implements ChannelPipelineFactory {
    private static final int MAX_FRAME_BYTES_LENGTH = Integer.MAX_VALUE;
    private static final int HEADER_BYTES = 4;
    private static final long MEMORY_SIZE = 1048576L;

    private final ChannelUpstreamHandler handler;
    private final MessageLite prototype;

    public PipelineFactory(final ChannelUpstreamHandler handler, final MessageLite prototype) {
        this.handler = handler;
        this.prototype = prototype;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        final ChannelPipeline p = Channels.pipeline();
        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_BYTES_LENGTH, 0, HEADER_BYTES, 0, HEADER_BYTES));

        final Executor eventExecutor =
                new OrderedMemoryAwareThreadPoolExecutor(16, MEMORY_SIZE, MEMORY_SIZE, 1000, MILLISECONDS, defaultThreadFactory());
        final ChannelHandler executionHandler = new ExecutionHandler(eventExecutor);

        final ExtensionRegistry extensionRegistry;
        if (prototype instanceof Message) {
            extensionRegistry = getExtensionRegistry(((MessageOrBuilder) prototype).getDescriptorForType().getFile());
        } else {
            extensionRegistry = null;
        }

        p.addLast("protobufDecoder", new ProtobufDecoder(prototype, extensionRegistry));

        p.addLast("frameEncoder", new LengthFieldPrepender(HEADER_BYTES));
        p.addLast("protobufEncoder", new ProtobufEncoder());
        p.addLast("executionHandler", executionHandler);
        p.addLast("handler", handler);
        return p;
    }
}
