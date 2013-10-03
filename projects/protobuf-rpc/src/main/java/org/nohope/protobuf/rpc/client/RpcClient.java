package org.nohope.protobuf.rpc.client;

import org.nohope.protobuf.core.net.PipelineFactory;
import org.nohope.rpc.protocol.RPC;
import com.google.protobuf.BlockingRpcChannel;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:04
 */
public final class RpcClient {
    private final ClientBootstrap bootstrap;

    private final RpcClientOptions options;

    public RpcClient(final RpcClientOptions options) {
        this.options = options;
        this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        this.bootstrap.setPipelineFactory(new PipelineFactory(new RpcClientHandler(),
                RPC.RpcResponse.getDefaultInstance()));
    }

    public BlockingRpcChannel connect() {
        return new RpcChannelImpl(bootstrap.connect(options.getAddress()).awaitUninterruptibly().getChannel(),
                options.getTimeout(), options.getTimeoutUnit());
    }

    public void shutdown() {
        bootstrap.releaseExternalResources();
    }
}
