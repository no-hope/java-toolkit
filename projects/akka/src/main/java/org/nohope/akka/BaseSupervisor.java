package org.nohope.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.nohope.typetools.JSON;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static org.nohope.akka.SupervisorRequests.StartupReply;


/**
 *
 *
 * Date: 25.07.12
 * Time: 12:14
 */
@SuppressFBWarnings(
        value = "UPM_UNCALLED_PRIVATE_METHOD",
        justification = "onConcreteMessage here is invoked "
                        + "reflectively to not to check types all the time")
public abstract class BaseSupervisor extends ReflectiveActor {
    private final Map<NamedWorkerMetadata, ActorRef> startingActors = new HashMap<>();

    protected BaseSupervisor() {
    }

    protected abstract Props newInputProps(final NamedWorkerMetadata inputClassId);

    @OnReceive
    public ActorRef obtainWorker(final NamedWorkerMetadata inputClassId) {
        return obtainWorkerRef(inputClassId);
    }

    @OnReceive
    public void processStartupReply(final StartupReply reply) {
        log.debug("Successful startup notification in {}: {}", getSelf().path().name(), JSON.pretty(reply));
        if (!startingActors.containsKey(reply.workerMetadata)) {
            throw new IllegalStateException("Request to remove non-existing actor: " + reply.workerMetadata);
        }
        startingActors.remove(reply.workerMetadata);
    }

    @Override
    public void postStop() {
        log.debug("Supervisor '{}' stopped", getSelf().path().name());
    }

    protected ActorRef obtainWorkerRef(final NamedWorkerMetadata inputClassId) {
        ActorRef deviceRef = startingActors.get(inputClassId);

        if (null == deviceRef) {
            final String url = getInputActorUrl(inputClassId);
            deviceRef = getContext().system().actorFor(url);

            if (deviceRef.isTerminated()) {
                final String actId = inputClassId.getIdentifier();
                log.debug("Starting new input processing actor with id {} in supervisor '{}'...", actId, getSelf().path().name());
                deviceRef = getContext().actorOf(newInputProps(inputClassId)
                        , actId);
                startingActors.put(inputClassId, deviceRef);
                deviceRef.tell(new SupervisorRequests.StartupRequest(), getSelf());
            } else {
                log.debug("Passing existing device actor...");
            }
        } else {
            log.debug("Passing existing device actor which is starting now...");
        }
        return deviceRef;
    }

    private String getInputActorUrl(final NamedWorkerMetadata inputClassId) {
        return MessageFormat.format("{0}/{1}"
                , getSelf().path()
                , inputClassId.getIdentifier());
    }
}

