package org.nohope.protobuf.rpc.client;

import org.nohope.rpc.protocol.RPC;
import org.nohope.protobuf.core.net.PipelineFactory;
import com.google.protobuf.BlockingRpcChannel;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:04
 */
public final class RpcClient {
    private final ClientBootstrap bootstrap;

    private final long timeout;
    private final TimeUnit unit;

    public RpcClient(final long timeout, final TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
        bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new PipelineFactory(new RpcClientHandler(), RPC.RpcResponse.getDefaultInstance()));
    }

    public BlockingRpcChannel connect(final SocketAddress sa) {
        return new RpcChannelImpl(bootstrap.connect(sa).awaitUninterruptibly().getChannel(), timeout, unit);
    }

    public void shutdown() {
        bootstrap.releaseExternalResources();
    }
}
