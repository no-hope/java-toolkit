package org.nohope.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 4:43 PM
 */
public class Controller implements RpcController {
    private final AtomicReference<String> reason = new AtomicReference<>();
    private final AtomicBoolean failed = new AtomicBoolean();
    private final AtomicBoolean canceled = new AtomicBoolean();
    private final AtomicReference<RpcCallback<Object>> callback = new AtomicReference<>();

    @Override
    public String errorText() {
        return reason.get();
    }

    @Override
    public boolean failed() {
        return failed.get();
    }

    @Override
    public boolean isCanceled() {
        return canceled.get();
    }

    @Override
    public void notifyOnCancel(final RpcCallback<Object> callback) {
        this.callback.set(callback);
    }

    @Override
    public void reset() {
        reason.set(null);
        failed.set(false);
        canceled.set(false);
        callback.set(null);
    }

    @Override
    public void setFailed(final String reason) {
        this.reason.set(reason);
        this.failed.set(true);
    }

    @Override
    public void startCancel() {
        this.canceled.set(true);
    }
}
