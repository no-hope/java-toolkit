package org.nohope.protobuf.rpc.client;

import com.google.protobuf.BlockingRpcChannel;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:04
 */
public interface IRpcClient {
    BlockingRpcChannel connect();
    void shutdown();
    boolean isAvailable();
}
