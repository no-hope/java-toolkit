package org.nohope.service.rpc;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/21/13 3:42 PM
 */

import org.nohope.protocol.RPC;
import org.nohope.service.rpc.exeption.InvalidRpcRequestException;
import org.nohope.service.rpc.exeption.NoSuchServiceException;
import org.nohope.service.rpc.exeption.NoSuchServiceMethodException;
import org.nohope.service.rpc.exeption.RpcException;
import org.nohope.service.rpc.exeption.RpcServiceException;
import org.nohope.service.rpc.exeption.ServerSideException;
import com.google.protobuf.BlockingService;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelUpstreamHandler implements IServiceRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(RpcServerHandler.class);
    private final Map<String, BlockingService> blockingServiceMap = new ConcurrentHashMap<>();

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

        final RPC.RpcRequest request = (RPC.RpcRequest) e.getMessage();
        final String serviceName = request.getServiceName();
        final String methodName = request.getMethodName();

        LOG.info("Received request for serviceName: {}, method: {}", serviceName, methodName);

        final BlockingService blockingService = blockingServiceMap.get(serviceName);
        if (blockingService == null) {
            throw new NoSuchServiceException(request, serviceName);
        }
        if (blockingService.getDescriptorForType().findMethodByName(methodName) == null) {
            throw new NoSuchServiceMethodException(request, methodName);
        }

        final MethodDescriptor methodDescriptor = blockingService.getDescriptorForType().findMethodByName(methodName);

        final Message methodRequest;
        try {
            methodRequest = buildMessageFromPrototype(
                    blockingService.getRequestPrototype(methodDescriptor),
                    request.getPayload());
        } catch (InvalidProtocolBufferException ex) {
            throw new InvalidRpcRequestException(ex, request, "Could not build method request message");
        }
        final RpcController controller = new Controller();

        final Message methodResponse;
        try {
            methodResponse = blockingService.callBlockingMethod(methodDescriptor, controller, methodRequest);
        } catch (ServiceException ex) {
            throw new RpcServiceException(ex, request, "BlockingService RPC call threw ServiceException");
        } catch (Exception ex) {
            throw new RpcException(ex, request, "BlockingService threw unexpected exception");
        }
        if (controller.failed()) {
            throw new RpcException(request, "BlockingService RPC failed: " + controller.errorText());
        } else if (methodResponse == null) {
            throw new RpcException(request, "BlockingService RPC returned null response");
        }
        final RPC.RpcResponse response =
                RPC.RpcResponse.newBuilder()
                   .setId(request.getId())
                   .setPayload(methodResponse.toByteString())
                   .build();

        writeResponse(e.getChannel(), response);
    }

    private static void writeResponse(final Channel c, final RPC.RpcResponse response) {
        c.write(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOG.debug("Message '{}' successfully sent", response);
                } else if (future.getCause() != null) {
                    LOG.warn("Unable to send message '{}' ({})", response, future.getCause());
                } else {
                    LOG.debug("ChannelFuture for writing message '{}' complete "
                              + "unsuccessfully with unknown throwable (cancelled={})",
                            response, future.isCancelled());
                }
            }
        });
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
        final Throwable cause = e.getCause();
        LOG.warn("exceptionCaught", cause);

        final RPC.RpcResponse.Builder responseBuilder = RPC.RpcResponse.newBuilder();
        if (!(cause instanceof ServerSideException)) {
            /* Cannot respond to this exception, because it is not tied
             * to a request */
            LOG.info("Cannot respond to handler exception", cause);
            return;
        }

        responseBuilder.setErrorCode(RPC.ErrorCode.SERVICE_NOT_FOUND);
        final String message = cause.getMessage();
        if (message != null) {
            responseBuilder.setErrorMessage(message);
        }

        final ServerSideException ex = (ServerSideException) cause;
        if (ex.getRequest() != null && ex.getRequest().hasId()) {
            responseBuilder.setId(ex.getRequest().getId());
            responseBuilder.setErrorMessage(ex.getMessage());
            writeResponse(e.getChannel(), responseBuilder.build());
        } else {
            LOG.info("Cannot respond to handler exception", ex);
        }
    }

    private static Message buildMessageFromPrototype(final Message prototype,
                                                     final ByteString messageToBuild)
            throws InvalidProtocolBufferException {
        return prototype.newBuilderForType()
                        .mergeFrom(messageToBuild)
                        .build();
    }

    @Override
    public void registerService(final BlockingService service) {
        if(blockingServiceMap.containsKey(service.getDescriptorForType().getFullName())) {
            throw new IllegalArgumentException("BlockingService already registered");
        }
        blockingServiceMap.put(service.getDescriptorForType().getFullName(), service);
    }

    @Override
    public void unregisterService(final BlockingService service) {
        if(!blockingServiceMap.containsKey(service.getDescriptorForType().getFullName())) {
            throw new IllegalArgumentException("BlockingService not already registered");
        }
        blockingServiceMap.remove(service.getDescriptorForType().getFullName());
    }
}
