package org.nohope.spring;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.nohope.test.SerializationUtils;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 11:11 PM
 */
public class PartiallyDefinedArgumentsFactoryTest {
    @Test
    public void partialInject() throws Exception {
        final Map<Map.Entry<GenericApplicationContext, Integer>,
                PartiallyDefinedArgumentsFactory<Bean>> factoryMap = new HashMap<>();

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            SpringUtils.registerSingleton(ctx, 1);

            final PartiallyDefinedArgumentsFactory<Bean> factory1 =
                    PartiallyDefinedArgumentsFactory.create(ctx, Bean.class);
            factory1.addBean("param2", "test1");
            factory1.addBean("param3", "test2");
            factoryMap.put(new ImmutablePair<>(ctx, 0), factory1);
        }

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            SpringUtils.registerSingleton(ctx, 1);

            final PartiallyDefinedArgumentsFactory<Bean> factory1 =
                    new PartiallyDefinedArgumentsFactory<>(ctx, Bean.class);
            factory1.addBean("param2", "test1");
            factory1.addBean("param3", "test2");
            factoryMap.put(new ImmutablePair<>(ctx, 0), factory1);
        }

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            final Map<String, Object> map = new HashMap<>();
            map.put("param2", "test1");
            map.put("param3", "test2");

            final PartiallyDefinedArgumentsFactory<Bean> factory2 =
                    new PartiallyDefinedArgumentsFactory<>(ctx, Bean.class, Arrays.<Object> asList(1), map);
            factoryMap.put(new ImmutablePair<>(ctx, 1), factory2);
        }

        {
            final GenericApplicationContext ctx = new GenericApplicationContext();
            final PartiallyDefinedArgumentsFactory<Bean> factory3 =
                    new PartiallyDefinedArgumentsFactory<>(ctx, Bean.class);
            factory3.addBeans(1);
            factory3.addBean("param2", "test1");
            factory3.addBean("param3", "test2");
            factoryMap.put(new ImmutablePair<>(ctx, 1), factory3);
        }

        for (Map.Entry<Map.Entry<GenericApplicationContext, Integer>, PartiallyDefinedArgumentsFactory<Bean>> e : factoryMap.entrySet()) {
            final PartiallyDefinedArgumentsFactory<Bean> beanFactory = e.getValue();

            final Bean bean = beanFactory.instantiate();

            assertEquals(1, (int) bean.getParam1());
            assertEquals("test1", bean.getParam2());
            assertEquals("test2", bean.getParam3());

            final Map<String,Object> namedBeans = beanFactory.getNamedBeans();
            assertTrue(namedBeans.containsKey("param2") && namedBeans.containsKey("param3"));
            final List<Object> beans = beanFactory.getBeans();
            assertEquals((int) e.getKey().getValue(), beans.size());
            assertEquals(Bean.class, beanFactory.getTargetClass());

            assertEquals(e.getKey().getKey(), beanFactory.getContext());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullBeanAdding() {
        PartiallyDefinedArgumentsFactory
                .create(new GenericApplicationContext(), Bean.class)
                .addBeans("1", null);
    }

    @Test(expected = AssertionError.class)
    public void javaSerialization() {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        final PartiallyDefinedArgumentsFactory<Bean> beanFactory = new PartiallyDefinedArgumentsFactory<>(ctx, Bean.class);

        SerializationUtils.cloneJava(beanFactory);
    }
}
