package org.nohope.spring;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.nohope.test.SerializationUtils;

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
        final GenericApplicationContext ctx = new GenericApplicationContext();
        SpringUtils.registerSingleton(ctx, 1);

        final PartiallyDefinedArgumentsFactory<Bean> beanFactory = new PartiallyDefinedArgumentsFactory<>(ctx, Bean.class);
        beanFactory.addBean("param2", "test1");
        beanFactory.addBean("param3", "test2");

        final Bean bean = beanFactory.instantiate();

        assertEquals(1, (int) bean.getParam1());
        assertEquals("test1", bean.getParam2());
        assertEquals("test2", bean.getParam3());

        assertEquals(ctx, beanFactory.getContext());
        final Map<String,Object> namedBeans = beanFactory.getNamedBeans();
        assertTrue(namedBeans.containsKey("param2") && namedBeans.containsKey("param3"));
        final List<Object> beans = beanFactory.getBeans();
        assertEquals(0, beans.size());
        assertEquals(Bean.class, beanFactory.getTargetClass());
    }

    @Test(expected = AssertionError.class)
    public void javaSerialization() {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        final PartiallyDefinedArgumentsFactory<Bean> beanFactory = new PartiallyDefinedArgumentsFactory<>(ctx, Bean.class);

        SerializationUtils.cloneJava(beanFactory);
    }
}
