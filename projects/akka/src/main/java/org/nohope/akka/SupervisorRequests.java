package org.nohope.akka;

import org.nohope.domain.device.DeviceId;

/**
 * Date: 07.08.12
 * Time: 15:49
 */
public final class SupervisorRequests {
    private SupervisorRequests() {
    }

    public static class StartupRequest {

    }

    private static class BaseDeviceRequest {
        protected final DeviceId deviceId;

        public BaseDeviceRequest(final DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        public DeviceId getDeviceId() {
            return deviceId;
        }
    }

    public static class StartupReply extends BaseDeviceRequest {

        public StartupReply(final DeviceId deviceId) {
            super(deviceId);
        }

    }
}
