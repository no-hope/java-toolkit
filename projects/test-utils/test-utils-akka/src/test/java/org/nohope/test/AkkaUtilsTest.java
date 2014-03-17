package org.nohope.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/4/12 12:27 PM
 */
public class AkkaUtilsTest extends UtilitiesTestSupport {

    @Test
    public void actorUri() {
        final Pattern exp = Pattern.compile("akka://test@localhost:\\d+/user/testActor");

        final URI uri = AkkaUtils.generateActorUri("test", "localhost", "testActor");
        assertTrue(exp.matcher(uri.toString()).matches());

        final Pattern exp2 = Pattern.compile("akka://test@[^:]+:\\d+/user/testActor");
        final URI uri2 = AkkaUtils.generateLocalHostActorUri("test", "testActor");
        assertTrue(exp2.matcher(uri2.toString()).matches());

        try {
            AkkaUtils.generateActorUri("test", "localhost", "\ntestActor");
            fail();
        } catch (final IllegalArgumentException ignored) {
        }
    }

    @Test
    public void actorSystemCreation() {
        AkkaUtils.createLocalSystem("test");
        AkkaUtils.createRemoteSystem("test");
    }

    @Test
    public void resourceAvailability() throws IOException {
        assertNotNull(ResourceUtils.getResourceAsString("/test/akka.conf"));
    }

    @Test
    public void builderTest() {
        final AkkaUtils.ActorSystemBuilder builder =
                AkkaUtils.buildRemoteSystem("testing", "localhost", 2560)
                     .put("loglevel", "INFO")

                     .buildEntry("actor")
                        .buildEntry("default-dispatcher")
                        .put("throughput", "50")
                            .buildEntry("fork-join-executor")
                            .put("parallelism-min", "32")
                            .put("parallelism-max", "64")
                            .put("parallelism-factor", "3.0")
                        .finishEntry() // not necessary
                     .end()

                     .buildEntry("remote")
                         .put("log-sent-messages", "off")
                         .put("log-received-messages", "off")
                         .put("log-remote-lifecycle-events", "on")
                     .end()
                ;

        final Config config = ConfigFactory.parseString(builder.joinWithDefaultConfig());
        assertEquals(config.getString("akka.actor.default-dispatcher.throughput"), "50");
        assertEquals(config.getString("akka.actor.default-dispatcher.fork-join-executor.parallelism-max"), "64");
        assertEquals(config.getString("akka.loglevel"), "INFO");
        assertEquals(config.getString("akka.remote.log-sent-messages"), "off");
        assertEquals(config.getInt("akka.remote.netty.port"), 2560);

        try {
            AkkaUtils.buildRemoteSystem("testing", "localhost", 2560)
                     .buildEntry("xxx")
                     .finishEntry();
            fail();
        } catch (final IllegalStateException e) {
        }
    }

    @Override
    protected Class<?> getUtilityClass() {
        return AkkaUtils.class;
    }
}
