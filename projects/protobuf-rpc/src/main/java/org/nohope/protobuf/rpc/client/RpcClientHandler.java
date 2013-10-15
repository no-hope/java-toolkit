package org.nohope.protobuf.rpc.client;

import org.nohope.rpc.protocol.RPC;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.nohope.protobuf.rpc.client.RpcChannelImpl.ResponsePrototypeRpcCallback;
import static org.jboss.netty.channel.ChannelHandler.Sharable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:25
 */
@Sharable
class RpcClientHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RpcClientHandler.class);
    private final AtomicInteger seqNum = new AtomicInteger(0);
    private final Map<Integer, ResponsePrototypeRpcCallback> callbackMap = new ConcurrentHashMap<>();

    public int getNextSeqId() {
        return seqNum.getAndIncrement();
    }

    public synchronized void registerCallback(final int seqId,
                                              final ResponsePrototypeRpcCallback callback) {
        if (callbackMap.containsKey(seqId)) {
            throw new IllegalArgumentException("Callback already registered");
        }
        callbackMap.put(seqId, callback);
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) {
        LOG.info("Channel connected");
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        final RPC.RpcResponse response = (RPC.RpcResponse) e.getMessage();

        if (!response.hasId()) {
            LOG.debug("Should never receive response without seqId");
            return;
        }

        final int seqId = response.getId();
        final ResponsePrototypeRpcCallback callback = callbackMap.remove(seqId);

        if (response.hasError() && callback != null && callback.getRpcController() != null) {
            callback.getRpcController().setFailed(response.getError().getErrorMessage());
        }

        if (callback == null) {
            LOG.debug("Received response with no callback registered");
        } else {
            LOG.debug("Invoking callback with response");
            callback.run(response);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx,
                                final ExceptionEvent e) throws Exception {
        LOG.error("Unhandled exception in handler", e.getCause());
        e.getChannel().close();
        throw new Exception(e.getCause());
    }
}
