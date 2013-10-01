package org.nohope.protobuf.rpc.client;

import org.nohope.protobuf.rpc.server.Controller;
import org.nohope.rpc.protocol.RPC;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.jboss.netty.channel.Channel;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import static com.google.protobuf.Descriptors.MethodDescriptor;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:09
 */
public class RpcChannel implements com.google.protobuf.RpcChannel, BlockingRpcChannel {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannel.class);

    private final Channel channel;
    private final RpcClientHandler handler;

    public RpcChannel(final Channel channel) {
        this.channel = channel;
        this.handler = channel.getPipeline().get(RpcClientHandler.class);
        if (handler == null) {
            throw new IllegalArgumentException("Channel does not have proper handler");
        }
    }

    public RpcController newRpcController() {
        return new Controller();
    }

    @Override
    public Message callBlockingMethod(final MethodDescriptor method,
                                      final RpcController controller,
                                      final Message request,
                                      final Message responsePrototype) throws ServiceException {
        final BlockingRpcCallback callback = new BlockingRpcCallback();
        final ResponsePrototypeRpcCallback rpcCallback =
                new ResponsePrototypeRpcCallback(controller, responsePrototype, callback);

        final int nextSeqId = handler.getNextSeqId();
        final Message rpcRequest = buildRequest(true, nextSeqId, true, method, request);
        handler.registerCallback(nextSeqId, rpcCallback);
        channel.write(rpcRequest);
        synchronized(callback) {
            while(!callback.isDone()) {
                try {
                    callback.wait();
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted while blocking", e);
                }
            }
        }

        if (rpcCallback.getRpcResponse() != null && rpcCallback.getRpcResponse().hasError()) {
            if (rpcCallback.controller instanceof Controller) {
                final RPC.Error error = ((Controller) rpcCallback.controller).getError();
                if (error != null) {
                    throw new DetailedExpectedException(error);
                }
            }

            // TODO: should we only throw this if the error code matches the
            // case where the server call threw a ServiceException?
            throw new ServiceException(rpcCallback.getRpcResponse().getError().getErrorMessage());
        }
        return callback.getMessage();
    }

    private static Message buildRequest(final boolean hasSequence,
                                        final int seqId,
                                        final boolean isBlocking,
                                        final MethodDescriptor method,
                                        final Message request) {
        final RPC.RpcRequest.Builder requestBuilder = RPC.RpcRequest.newBuilder();
        //if (hasSequence) {
            requestBuilder.setId(seqId);
        //}
        return requestBuilder
                //.setIsBlockingService(isBlocking)
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

        private final RpcController controller;
        private final Message responsePrototype;
        private final RpcCallback<Message> callback;

        private RPC.RpcResponse rpcResponse;

        public ResponsePrototypeRpcCallback(final RpcController controller,
                                            final Message responsePrototype,
                                            final RpcCallback<Message> callback) {
            if (responsePrototype == null) {
                throw new IllegalArgumentException("Must provide response prototype");
            } else if (callback == null) {
                throw new IllegalArgumentException("Must provide callback");
            }
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
                if (controller != null) {
                    if (controller instanceof Controller) {
                        ((Controller) controller).setError(message.getError());
                    } else {
                        controller.setFailed(message.getError().getErrorMessage());
                    }
                }
                callback.run(message);
                return;
            }

            try {
                final Message response =
                        responsePrototype.newBuilderForType()
                                         .mergeFrom(message.getPayload()).build();
                callback.run(response);
            } catch (final InvalidProtocolBufferException e) {
                LOG.warn("Could not marshall into response", e);
                if (controller != null) {
                    controller.setFailed("Received invalid response type from server");
                }
                callback.run(null);
            }
        }

        public RpcController getRpcController() {
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
