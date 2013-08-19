package org.nohope.service.handler;

import com.google.protobuf.MessageLite;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/19/13 7:06 PM
 */
public class ProtobufRpcHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ProtobufRpcHandler.class);

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        final Object message = e.getMessage();

        // TODO: do stuff
        for (final Method method : this.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(OnReceive.class) == null) {
                continue;
            }

            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == message.getClass()) {
                final Object result = method.invoke(this, message);

                if (result instanceof MessageLite) {
                    ctx.getChannel().write(result).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(final ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                LOG.debug("Message '{}' successfully sent", result);
                            } else if (future.getCause() != null) {
                                LOG.warn("Unable to send message '{}' ({})", result, future.getCause());
                            } else {
                                LOG.debug("ChannelFuture for writing message '{}' complete "
                                          + "unsuccessfully with unknown throwable (cancelled={})",
                                        result, future.isCancelled());
                            }
                        }
                    });
                }
                return;
            }
        }
    }
}
