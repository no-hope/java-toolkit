package org.nohope.spring;

import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.inject.Inject;

import static org.junit.Assert.*;
import static org.nohope.spring.SpringUtils.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-14 18:22
 */
public class SpringUtilsTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return SpringUtils.class;
    }

    @Test
    public void inheriting() {
        final ConfigurableApplicationContext ctx =
                ensureCreate(propagateAnnotationProcessing(
                        new ClassPathXmlApplicationContext("parent.xml")), "child.xml");

        assertNotNull(ctx);
        assertEquals("parent_only", ctx.getBean("parent_only", String.class));
        assertEquals("imported_imported", ctx.getBean("parent_imported", String.class));
        assertEquals("imported", ctx.getBean("imported", String.class));
        assertEquals("child", ctx.getBean("child", String.class));
        assertEquals("child", ctx.getBean("parent", String.class));
        assertEquals("imported_child", ctx.getBean("imported_parent", String.class));
    }

    @Test
    public void inheritingNonExistent() {
        final ConfigurableApplicationContext ctx =
                ensureCreate(propagateAnnotationProcessing(
                        new ClassPathXmlApplicationContext("parent.xml")), "nonexisting.xml");

        assertNull(ctx);
    }

    @Test
    public void postInject() {
        final ConfigurableApplicationContext ctx =
                propagateAnnotationProcessing(new GenericApplicationContext());

        final PostInjectedBean bean = new PostInjectedBean();
        assertNull(bean.value);
        SpringUtils.registerSingleton(ctx, "test");
        SpringUtils.setProperties(ctx, bean);
        assertEquals("test", bean.value);
    }

    public static class PostInjectedBean {
        @Inject private String value;
    }

    @Test
    public void qualifyingInject() {
        final ConfigurableApplicationContext ctx =
                propagateAnnotationProcessing(new GenericApplicationContext());

        final NamedBean b1 = new NamedBean("bean1");
        final NamedBean b2 = new NamedBean("bean2");


        registerSingleton(ctx, "bean1", b1, SpecialQualifier.class, Type.TYPE1);
        registerSingleton(ctx, "bean2", b2, SpecialQualifier.class, Type.TYPE2);

        final Q1 q1 = getOrInstantiate(ctx, Q1.class);
        final Q2 q2 = getOrInstantiate(ctx, Q2.class);

        assertSame(b1, q1.getBean());
        assertSame(b2, q2.getBean());
    }

    @Test
    public void singleton() {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        final Bean bean = new Bean();
        SpringUtils.registerSingleton(ctx, bean);
        assertSame(bean, getOrInstantiate(ctx, Bean.class));
        assertSame(getOrInstantiate(ctx, Bean.class), getOrInstantiate(ctx, Bean.class));

        final Bean bean2 = new Bean();
        SpringUtils.registerSingleton(ctx, "test2", bean2);
        assertSame(bean2, ctx.getBean("test2"));

        final Bean bean3 = new Bean();
        SpringUtils.registerSingleton(ctx, "test3", bean3);
        assertSame(bean3, ctx.getBean("test3"));
    }

    @Test
    public void annotationProcessing() {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        final Bean bean = new Bean();
        SpringUtils.registerSingleton(ctx, bean);

        try {
            getOrInstantiate(ctx, Bean2.class);
        } catch (final BeanCreationException ignored) {
        }

        propagateAnnotationProcessing(ctx);
        assertNotSame(getOrInstantiate(ctx, Bean2.class), getOrInstantiate(ctx, Bean2.class));
        assertSame(bean, getOrInstantiate(ctx, Bean2.class).getBean());
    }

    private static final class Bean {
    }

    private static final class Bean2 {
        private final Bean bean;

        @Inject
        private Bean2(final Bean bean) {
            this.bean = bean;
        }

        private Bean getBean() {
            return bean;
        }
    }

    public static class NamedBean {
        private final String name;

        public NamedBean(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class AbstractBeanWithQualifier {
        private final NamedBean bean;

        public AbstractBeanWithQualifier(final NamedBean bean) {
            this.bean = bean;
        }

        public NamedBean getBean() {
            return bean;
        }
    }

    public static class Q1 extends AbstractBeanWithQualifier {
        @Inject
        public Q1(@SpecialQualifier(Type.TYPE1) final NamedBean bean) {
            super(bean);
        }
    }

    public static class Q2 extends AbstractBeanWithQualifier {
        @Inject
        public Q2(@SpecialQualifier(Type.TYPE2) final NamedBean bean) {
            super(bean);
        }
    }
}
