package org.nohope.akka;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Date: 9/21/12
 * Time: 10:53 AM
 */
public abstract class BaseWorkerActor extends UntypedActor {
    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    protected final NamedWorkerMetadata workerMetadata;

    protected BaseWorkerActor(final NamedWorkerMetadata workerMetadata) {
        this.workerMetadata = workerMetadata;
    }

    @SuppressWarnings("unused")
    private void onConcreteMessage(final SupervisorRequests.StartupRequest request) {
        log.debug("Sending startup notification to supervisor");
        final SupervisorRequests.StartupReply reply = new SupervisorRequests.StartupReply(workerMetadata);
        getSender().tell(reply);
    }
}
