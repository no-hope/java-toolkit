package org.nohope.rpc;

import com.google.protobuf.BlockingService;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 5:09 PM
 */
public class RpcServer implements IServiceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(RpcServer.class);

    private final RpcServerHandler handler = new RpcServerHandler();
    private final ServerBootstrap bootstrap;

    public RpcServer() {
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new PipelineFactory(handler));
    }

    public void bind(final InetSocketAddress address) {
        bootstrap.bind(address);
        LOG.debug("Service is bind to {}", address);
    }

    @Override
    public void registerService(final BlockingService service) {
        handler.registerService(service);
    }

    @Override
    public void unregisterService(final BlockingService service) {
        handler.unregisterService(service);
    }
}
