package org.nohope.rpc;

import org.nohope.protobuf.core.Controller;
import org.nohope.protobuf.core.exception.DetailedExpectedException;
import org.nohope.protobuf.core.exception.ExpectedServiceException;
import org.nohope.protobuf.core.exception.RpcTimeoutException;
import org.nohope.protobuf.rpc.client.RpcClient;
import org.nohope.protobuf.rpc.server.RpcServer;
import org.nohope.protocol.TestService;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.junit.Test;
import org.nohope.test.SocketUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.nohope.protocol.TestService.Service.BlockingInterface;
import static org.nohope.protocol.TestService.TestServiceErrorCode.TEST_ERROR;
import static org.nohope.protocol.TestService.detailedResason;
import static org.junit.Assert.*;

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

            if ("timeout".contains(data)) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException ignored) {
                }
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

        final RpcClient client = new RpcClient(2, TimeUnit.SECONDS);

        final BlockingRpcChannel channel = client.connect(address);
        final BlockingInterface stub = TestService.Service.newBlockingStub(channel);
        final RpcController controller = new Controller();

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

        try {
            stub.ping(controller, TestService.Ping.newBuilder().setData("timeout").build());
            fail();
        } catch (final RpcTimeoutException e) {
            assertNotNull(e.getCause());
        } catch (final ServiceException e) {
            fail();
        }
    }
}
