package org.nohope.akka.spring;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.nohope.spring.SpringUtils;

import static org.junit.Assert.assertEquals;
import static org.nohope.akka.Ask.waitReply;
import static org.nohope.akka.spring.Bean.Props.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 11:11 PM
 */
public class ReflectActorFactoryTest {
    @Test
    public void partialInject() throws Exception {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        SpringUtils.registerSingleton(ctx, 1);

        final ReflectActorFactory<Bean> beanFactory = new ReflectActorFactory<>(ctx, Bean.class);
        beanFactory.addBean("param2", "test1");
        beanFactory.addBean("param3", "test2");

        final ActorSystem system = ActorSystem.create();
        final ActorRef ref = system.actorOf(new Props(beanFactory));

        Assert.assertEquals(1, waitReply(ref, PARAM1));
        Assert.assertEquals("test1", waitReply(ref, PARAM2));
        Assert.assertEquals("test2", waitReply(ref, PARAM3));
    }
}
