package org.nohope.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/4/12 12:27 PM
 */
public class AkkaUtilsTest {
    @Test
    public void actorSystemCreation() {
        AkkaUtils.createLocalSystem("test");
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
    }
}
