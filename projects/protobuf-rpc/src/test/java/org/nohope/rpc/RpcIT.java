package org.nohope.rpc;

import org.nohope.protobuf.rpc.client.DetailedExpectedException;
import org.nohope.protobuf.rpc.client.RpcChannel;
import org.nohope.protobuf.rpc.client.RpcClient;
import org.nohope.protobuf.rpc.core.RpcServer;
import org.nohope.protobuf.rpc.exception.ExpectedServiceException;
import org.nohope.protocol.TestService;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.junit.Test;
import org.nohope.test.SocketUtils;

import java.net.InetSocketAddress;

import static org.nohope.protocol.TestService.Service.BlockingInterface;
import static org.nohope.protocol.TestService.TestServiceErrorCode.TEST_ERROR;
import static org.nohope.protocol.TestService.detailedResason;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:41
 */
public class RpcIT {

    public static class ServiceImpl implements BlockingInterface {
        @Override
        public TestService.Pong ping(final RpcController controller, final TestService.Ping request)
                throws ServiceException {

            final String data = request.getData();
            if ("fail".contains(data)) {
                throw ExpectedServiceException.wrap(new Throwable(), detailedResason, TEST_ERROR);
            }

            return TestService.Pong.newBuilder().setData(data).build();
        }
    }

    @Test
    public void blockingInterfaceTest() {
        final InetSocketAddress address = SocketUtils.getAvailableLocalAddress();

        final RpcServer server = new RpcServer();
        server.registerService(TestService.Service.newReflectiveBlockingService(new ServiceImpl()));
        server.bind(address);

        final RpcClient client = new RpcClient();

        final RpcChannel channel = client.connect(address);
        final BlockingInterface stub = TestService.Service.newBlockingStub(channel);
        final RpcController controller = channel.newRpcController();

        try {
            final TestService.Pong pong = stub.ping(controller, TestService.Ping.newBuilder().setData("test").build());
            assertEquals("test", pong.getData());
        } catch (final ServiceException e) {
            fail(e.getMessage());
        }

        try {
            stub.ping(controller, TestService.Ping.newBuilder().setData("fail").build());
            fail();
        } catch (final DetailedExpectedException e) {
            assertEquals(TEST_ERROR, e.getDetailedReason(detailedResason));
        } catch (final ServiceException e) {
            fail();
        }
    }
}
