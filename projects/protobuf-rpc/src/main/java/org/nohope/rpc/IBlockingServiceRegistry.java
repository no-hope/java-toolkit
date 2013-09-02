package org.nohope.rpc;

import com.google.protobuf.BlockingService;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 5:10 PM
 */
public interface IBlockingServiceRegistry {
    void registerService(final BlockingService service);

    void unregisterService(final BlockingService service);
}
