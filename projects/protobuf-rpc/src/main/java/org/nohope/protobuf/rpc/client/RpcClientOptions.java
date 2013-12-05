package org.nohope.protobuf.rpc.client;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-02 14:40
 */
public class RpcClientOptions {
    private final InetSocketAddress address;
    private final long timeout;
    private final TimeUnit timeoutUnit;

    public RpcClientOptions(@Nonnull final InetSocketAddress address,
                            final long timeout,
                            @Nonnull final TimeUnit timeoutUnit) {
        this.address = address;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    public static class Builder {
        private final InetSocketAddress address;
        private long timeout = 30;
        private TimeUnit timeoutUnit = TimeUnit.SECONDS;

        public Builder(@Nonnull final InetSocketAddress address) {
            this.address = address;
        }

        public Builder setTimeout(final long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setTimeoutUnit(@Nonnull final TimeUnit timeoutUnit) {
            this.timeoutUnit = timeoutUnit;
            return this;
        }

        public RpcClientOptions build() {
            return new RpcClientOptions(address, timeout, timeoutUnit);
        }
    }

    @Nonnull
    public InetSocketAddress getAddress() {
        return address;
    }

    public long getTimeout() {
        return timeout;
    }

    @Nonnull
    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }
}
