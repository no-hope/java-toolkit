package org.nohope.akka;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Date: 07.08.12
 * Time: 15:49
 */
public final class SupervisorRequests {
    private SupervisorRequests() {
    }

    private static class BaseSupervisorRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        protected final NamedWorkerMetadata workerMetadata;

        /* for json serialization only */
        @SuppressWarnings("unused")
        private BaseSupervisorRequest() {
            this.workerMetadata = null;
        }

        public BaseSupervisorRequest(@Nonnull final NamedWorkerMetadata workerMetadata) {
            this.workerMetadata = workerMetadata;
        }

        @Nonnull
        public NamedWorkerMetadata getWorkerMetadata() {
            return workerMetadata;
        }
    }

    public static class StartupReply extends BaseSupervisorRequest {
        private static final long serialVersionUID = 1L;

        public StartupReply(@Nonnull final NamedWorkerMetadata workerMetadata) {
            super(workerMetadata);
        }

        /* for json serialization only */
        @SuppressWarnings("unused")
        private StartupReply() {
            super();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StartupReply)) {
                return false;
            }

            final BaseSupervisorRequest that = (BaseSupervisorRequest) o;
            return workerMetadata.equals(that.workerMetadata);
        }

        @Override
        public int hashCode() {
            return workerMetadata.hashCode();
        }
    }

    public static final class StartupRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StartupRequest)) {
                return false;
            }

            final StartupRequest that = (StartupRequest) o;
            return getClass().equals(that.getClass());
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }
}
