package org.nohope.akka.spring;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.nohope.reflection.IntrospectionUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.nohope.spring.SpringUtils;
import org.nohope.test.SerializationUtils;

import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.Entry;
import static org.junit.Assert.*;
import static org.nohope.akka.Ask.waitReply;
import static org.nohope.akka.spring.Bean.Props.*;
import static org.nohope.reflection.ModifierMatcher.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 11:11 PM
 */
public class SpringActorFactoryTest {
    @Test
    public void partialInject() throws Exception {
        final Map<Entry<GenericApplicationContext, Integer>,
                  SpringActorFactory<Bean>> factoryMap = new HashMap<>();

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            SpringUtils.registerSingleton(ctx, 1);

            final SpringActorFactory<Bean> factory1 = SpringActorFactory.createActorFactory(ctx, Bean.class);
            factory1.addBean("param2", "test1");
            factory1.addBean("param3", "test2");
            factoryMap.put(new ImmutablePair<>(ctx, 0), factory1);
        }

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            SpringUtils.registerSingleton(ctx, 1);

            final SpringActorFactory<Bean> factory1 =
                    new SpringActorFactory<>(ctx, Bean.class);
            factory1.addBean("param2", "test1");
            factory1.addBean("param3", "test2");
            factoryMap.put(new ImmutablePair<>(ctx, 0), factory1);
        }

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            final Map<String, Object> map = new HashMap<>();
            map.put("param2", "test1");
            map.put("param3", "test2");

            final SpringActorFactory<Bean> factory2 =
                    new SpringActorFactory<>(ctx, Bean.class, Arrays.<Object> asList(1), map);
            factoryMap.put(new ImmutablePair<>(ctx, 1), factory2);
        }

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            final SpringActorFactory<Bean> factory3 = new SpringActorFactory<>(ctx, Bean.class);
            factory3.addBeans(1);
            factory3.addBean("param2", "test1");
            factory3.addBean("param3", "test2");
            factoryMap.put(new ImmutablePair<>(ctx, 1), factory3);
        }

        for (final Entry<Entry<GenericApplicationContext, Integer>, SpringActorFactory<Bean>> e : factoryMap.entrySet()) {
            final SpringActorFactory<Bean> beanFactory = e.getValue();

            final ActorSystem system = ActorSystem.create();
            final ActorRef ref = system.actorOf(new Props(beanFactory));

            assertEquals(1, (int) waitReply(Integer.class, ref, PARAM1));
            assertEquals("test1", waitReply(String.class, ref, PARAM2));
            assertEquals("test2", waitReply(String.class, ref, PARAM3));

            final Map<String,Object> namedBeans = beanFactory.getNamedBeans();
            assertTrue(namedBeans.containsKey("param2") && namedBeans.containsKey("param3"));
            final List<Object> beans = beanFactory.getBeans();
            assertEquals((int) e.getKey().getValue(), beans.size());
            assertEquals(Bean.class, beanFactory.getTargetClass());

            assertNotNull(beanFactory.getProps());
            assertEquals(e.getKey().getKey(), beanFactory.getContext());
        }
    }

    @Test
    public void serialization() {
        final SpringActorFactory<Bean> f =
                new SpringActorFactory<>(new GenericApplicationContext(), Bean.class);

        try {
            IntrospectionUtils.invoke(f, ALL, "writeObject", new Object[] {null});
            fail();
        } catch (NoSuchMethodException | IllegalAccessException e) {
            fail();
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof NotSerializableException);
        }
    }

    @Test(expected = AssertionError.class)
    public void javaSerialization() {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        final SpringActorFactory<Bean> beanFactory = new SpringActorFactory<>(ctx, Bean.class);
        SerializationUtils.cloneJava(beanFactory);
    }
}
