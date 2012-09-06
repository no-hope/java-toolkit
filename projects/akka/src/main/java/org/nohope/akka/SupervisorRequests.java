package org.nohope.akka;

/**
 * Date: 07.08.12
 * Time: 15:49
 */
public final class SupervisorRequests {
    private SupervisorRequests() {
    }

    public static class StartupRequest {

        public StartupRequest() {
        }
    }

    private static class BaseSupervisorRequest {
        protected final NamedWorkerMetadata workerMetadata;

        public BaseSupervisorRequest(final NamedWorkerMetadata workerMetadata) {
            this.workerMetadata = workerMetadata;
        }

        public NamedWorkerMetadata getWorkerMetadata() {
            return workerMetadata;
        }
    }

    public static class StartupReply extends BaseSupervisorRequest {

        public StartupReply(final NamedWorkerMetadata workerMetadata) {
            super(workerMetadata);
        }

    }
}
