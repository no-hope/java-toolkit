package org.nohope.protobuf.rpc.client;

import com.google.protobuf.BlockingRpcChannel;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:04
 */
public interface IRpcClient {
    /**
     * @return connection to blocking service.
     */
    BlockingRpcChannel connect();

    /**
     * Performs graceful connection shutdown.
     */
    void shutdown();

    /**
     * Returns connection status. Please note, that {@code true} value may
     * return false-positive result due to asynchronous manner of message
     * exchanging full information on availability may appear only only on write
     * operation.
     *
     * @return {@code false} if client is server connection is not available.
     */
    boolean isServerAvailable();
}
