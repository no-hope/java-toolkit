package org.nohope.spring;

import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.GenericApplicationContext;

import javax.inject.Inject;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

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
    public void singleton() {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        final Bean bean = new Bean();
        SpringUtils.registerSingleton(ctx, bean);
        assertSame(bean, SpringUtils.getOrInstantiate(ctx, Bean.class));
        assertSame(SpringUtils.getOrInstantiate(ctx, Bean.class), SpringUtils.getOrInstantiate(ctx, Bean.class));

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
            SpringUtils.getOrInstantiate(ctx, Bean2.class);
        } catch (final BeanCreationException ignored) {
        }

        SpringUtils.propagateAnnotationProcessing(ctx);
        assertNotSame(SpringUtils.getOrInstantiate(ctx, Bean2.class), SpringUtils.getOrInstantiate(ctx, Bean2.class));
        assertSame(bean, SpringUtils.getOrInstantiate(ctx, Bean2.class).getBean());
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
}
