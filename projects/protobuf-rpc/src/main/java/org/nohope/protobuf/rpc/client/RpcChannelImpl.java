package org.nohope.protobuf.rpc.client;

import org.nohope.protobuf.core.Controller;
import org.nohope.protobuf.core.exception.DetailedExpectedException;
import org.nohope.protobuf.core.exception.RpcTimeoutException;
import org.nohope.rpc.protocol.RPC;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.jboss.netty.channel.Channel;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.protobuf.Descriptors.MethodDescriptor;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:09
 */
class RpcChannelImpl implements RpcChannel, BlockingRpcChannel {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelImpl.class);

    private final Channel channel;
    private final RpcClientHandler handler;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final long timeout;
    private final TimeUnit unit;

    public RpcChannelImpl(final Channel channel, final long timeout, final TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
        this.channel = channel;
        this.handler = channel.getPipeline().get(RpcClientHandler.class);
        if (handler == null) {
            throw new IllegalArgumentException("Channel does not have proper handler");
        }
    }

    public static RpcController newRpcController() {
        return new Controller();
    }

    @Override
    public Message callBlockingMethod(final MethodDescriptor method,
                                      @Nonnull final RpcController originController,
                                      final Message request,
                                      final Message responsePrototype) throws ServiceException {
        if (!(originController instanceof Controller)) {
            throw new IllegalArgumentException("Invalid controller type. You should RpcChannelImpl.newRpcController()");
        }

        final Controller controller = (Controller) originController;

        final BlockingRpcCallback callback = new BlockingRpcCallback();
        final ResponsePrototypeRpcCallback rpcCallback =
                new ResponsePrototypeRpcCallback(controller, responsePrototype, callback);

        final int nextSeqId = handler.getNextSeqId();
        final Message rpcRequest = buildRequest(nextSeqId, method, request);
        handler.registerCallback(nextSeqId, rpcCallback);
        channel.write(rpcRequest);

        final Future<Message> handler = executor.submit(new Callable<Message>(){
            @Override
            public Message call() throws Exception {
                synchronized(callback) {
                    while(!callback.isDone()) {
                        callback.wait();
                    }
                }

                if (rpcCallback.getRpcResponse() != null && rpcCallback.getRpcResponse().hasError()) {
                    final RPC.Error error = rpcCallback.controller.getError();
                    if (error != null) {
                        throw new DetailedExpectedException(error);
                    }

                    // TODO: should we only throw this if the error code matches the
                    // case where the server call threw a ServiceException?
                    throw new ServiceException(rpcCallback.getRpcResponse().getError().getErrorMessage());
                }
                return callback.getMessage();
            }
        });

        try {
            return handler.get(timeout, unit);
        } catch (ExecutionException e) {
            // TODO: more exceptional types
            final Throwable cause = e.getCause();
            if (cause instanceof ServiceException) {
                throw (ServiceException) cause;
            }
            throw new IllegalStateException(e);
        } catch (TimeoutException e) {
            throw new RpcTimeoutException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Message buildRequest(final int seqId,
                                        final MethodDescriptor method,
                                        final Message request) {
        final RPC.RpcRequest.Builder requestBuilder = RPC.RpcRequest.newBuilder();
        return requestBuilder
                .setId(seqId)
                .setServiceName(method.getService().getFullName())
                .setMethodName(method.getName())
                .setPayload(request.toByteString())
                .build();
    }

    @Override
    public void callMethod(final MethodDescriptor method,
                           final RpcController controller,
                           final Message request,
                           final Message responsePrototype,
                           final RpcCallback<Message> done) {
        throw new NoSuchMethodError("TBD");
    }

    static class ResponsePrototypeRpcCallback implements RpcCallback<RPC.RpcResponse> {

        private final Controller controller;
        private final Message responsePrototype;
        private final RpcCallback<Message> callback;

        private RPC.RpcResponse rpcResponse;

        public ResponsePrototypeRpcCallback(@Nonnull final Controller controller,
                                            @Nonnull final Message responsePrototype,
                                            @Nonnull final RpcCallback<Message> callback) {
            this.controller = controller;
            this.responsePrototype = responsePrototype;
            this.callback = callback;
        }

        @Override
        public void run(final RPC.RpcResponse message) {
            rpcResponse = message;

            if (message == null) {
                callback.run(null);
                return;
            }
            if (message.hasError()) {
                controller.setError(message.getError());
                callback.run(message);
                return;
            }

            try {
                final Message response =
                        responsePrototype.newBuilderForType()
                                         .mergeFrom(message.getPayload())
                                         .build();
                callback.run(response);
            } catch (final InvalidProtocolBufferException e) {
                LOG.warn("Could not marshall into response", e);
                controller.setFailed("Received invalid response type from server");
                callback.run(null);
            }
        }

        @Nonnull
        public Controller getRpcController() {
            return controller;
        }

        public RPC.RpcResponse getRpcResponse() {
            return rpcResponse;
        }
    }

    private static class BlockingRpcCallback implements RpcCallback<Message> {
        private boolean done = false;
        private Message message;

        @Override
        public void run(final Message message) {
            this.message = message;
            synchronized(this) {
                done = true;
                notify();
            }
        }

        public Message getMessage() {
            return message;
        }

        public boolean isDone() {
            return done;
        }

    }

}
