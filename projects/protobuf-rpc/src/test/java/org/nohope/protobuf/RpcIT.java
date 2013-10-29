package org.nohope.protobuf;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.junit.Test;
import org.nohope.protobuf.core.Controller;
import org.nohope.protobuf.core.exception.DetailedExpectedException;
import org.nohope.protobuf.core.exception.ExpectedServiceException;
import org.nohope.protobuf.core.exception.RpcTimeoutException;
import org.nohope.protobuf.core.exception.UnexpectedServiceException;
import org.nohope.protobuf.rpc.client.RpcClient;
import org.nohope.protobuf.rpc.client.RpcClientOptions;
import org.nohope.protobuf.rpc.server.RpcServer;
import org.nohope.protocol.TestService;
import org.nohope.rpc.protocol.RPC;
import org.nohope.test.SocketUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.nohope.protocol.TestService.Service.BlockingInterface;
import static org.nohope.protocol.TestService.TestServiceErrorCode.TEST_ERROR;
import static org.nohope.protocol.TestService.detailedResason;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 2013-10-01 15:41
 */
public class RpcIT {

    private static RpcServer createServer() {
        final RpcServer server = new RpcServer();
        final BlockingService service = TestService.Service.newReflectiveBlockingService(new ServiceImpl());
        server.registerService(service);

        return server;
    }

    @Test
    public void nonConnectedDuringClientCreating() {
        final InetSocketAddress address = SocketUtils.getAvailableLocalAddress();
        new RpcClient(new RpcClientOptions(address, 2, TimeUnit.SECONDS)).connect();
    }

    @Test
    public void reconnect() throws ServiceException {
        final InetSocketAddress address = SocketUtils.getAvailableLocalAddress();

        RpcServer server = createServer();
        server.bind(address);

        final RpcClient client = new RpcClient(new RpcClientOptions(address, 2, TimeUnit.SECONDS));

        final BlockingRpcChannel channel = client.connect();
        final BlockingInterface stub = TestService.Service.newBlockingStub(channel);

        final TestService.Ping ping = TestService.Ping.newBuilder().setData("test").build();
        stub.ping(null, ping);

        server.shutdown();

        try {
            stub.ping(null, ping);
            fail();
        } catch (UnexpectedServiceException e) {
        }

        server = createServer();
        server.bind(address);

        stub.ping(null, ping);

        server.shutdown();
        client.shutdown();
    }

    @Test
    public void blockingInterfaceTest() throws InterruptedException {
        final InetSocketAddress address = SocketUtils.getAvailableLocalAddress();

        final RpcServer server = new RpcServer();
        final BlockingService service = TestService.Service.newReflectiveBlockingService(new ServiceImpl());
        server.registerService(service);

        try {
            server.registerService(service);
            fail();
        } catch (final IllegalArgumentException ignored) {
        }

        server.bind(address);

        final RpcClient client = new RpcClient(new RpcClientOptions(address, 2, TimeUnit.SECONDS));

        final BlockingRpcChannel channel = client.connect();
        final BlockingInterface stub = TestService.Service.newBlockingStub(channel);

        try {
            final TestService.Pong pong = stub.ping(new Controller(),
                    TestService.Ping.newBuilder().setData("test").build());
            assertEquals("test", pong.getData());
        } catch (final ServiceException e) {
            fail(e.getMessage());
        }

        try {
            stub.ping(null, TestService.Ping.newBuilder().setData("fail").build());
            fail();
        } catch (final DetailedExpectedException e) {
            assertEquals(TEST_ERROR, e.getDetailedReason(detailedResason));
        } catch (final ServiceException e) {
            fail();
        }

        try {
            stub.ping(null, TestService.Ping.newBuilder().setData("timeout").build());
            fail();
        } catch (final RpcTimeoutException e) {
            assertNotNull(e.getCause());
        } catch (final ServiceException e) {
            fail();
        }

        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        server.unregisterService(service);
        try {
            server.unregisterService(service);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            stub.ping(null, TestService.Ping.newBuilder().setData("test").build());
            fail();
        } catch (final ServiceException e) {
            assertTrue(e instanceof DetailedExpectedException);
            assertEquals(RPC.ErrorCode.SERVICE_NOT_FOUND, ((DetailedExpectedException) e).getError().getErrorCode());
        }

        client.shutdown();
        server.shutdown();
    }

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
                    Thread.sleep(TimeUnit.SECONDS.toMillis(3));
                } catch (InterruptedException ignored) {
                }
            }

            return TestService.Pong.newBuilder().setData(data).build();
        }
    }
}
