package org.nohope.protobuf.rpc.client;

import org.nohope.rpc.protocol.RPC;
import org.nohope.protobuf.rpc.core.PipelineFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.SocketAddress;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:04
 */
public class RpcClient {
    private final ClientBootstrap bootstrap;

    public RpcClient() {
        bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new PipelineFactory(new RpcClientHandler(), RPC.RpcResponse.getDefaultInstance()));
    }

    public RpcChannel connect(final SocketAddress sa) {
        return new RpcChannel(bootstrap.connect(sa).awaitUninterruptibly().getChannel());
    }

    public void shutdown() {
        bootstrap.releaseExternalResources();
    }
}
