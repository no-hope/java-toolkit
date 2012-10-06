package org.nohope.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 10:40 PM
 */
public final class SpringUtils {
    private SpringUtils() {
    }

    /**
     * Overrides given context with sequential list of contexts specified by classpath file names.
     *
     * @param parent spring context
     * @param paths contexts path
     * @return overridden context ({@code null} if one of given files not exists)
     */
    public static ConfigurableApplicationContext ensureCreate(@Nullable final ConfigurableApplicationContext parent,
                                                              @Nonnull final String... paths) {
        for (final String path: paths) {
            final ClassPathResource config = new ClassPathResource(path);
            if (!config.exists()) {
                return null;
            }
        }

        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                paths.clone(),
                parent);
        return propagateAnnotationProcessing(ctx);
    }

    public static ConfigurableApplicationContext propagateAnnotationProcessing(
            final ConfigurableApplicationContext ctx) {
        final CommonAnnotationBeanPostProcessor beanPostProcessor = new CommonAnnotationBeanPostProcessor();
        beanPostProcessor.setBeanFactory(ctx.getBeanFactory());
        ctx.getBeanFactory().addBeanPostProcessor(beanPostProcessor);
        ctx.getBeanFactory().addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
        return ctx;
    }

    public static <T> T registerSingleton(final ConfigurableApplicationContext ctx,
                                          final String name,
                                          final T obj) {
        ctx.getBeanFactory().registerSingleton(name, obj);
        return obj;
    }

    public static <T> T registerSingleton(final ConfigurableApplicationContext ctx, @Nonnull final T obj) {
        ctx.getBeanFactory().registerSingleton(obj.getClass().getCanonicalName(), obj);
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(final ApplicationContext ctx,
                                    final Class<? extends T> clazz) {
        final AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
        return (T) factory.createBean(
                clazz,
                AutowireCapableBeanFactory.AUTOWIRE_NO,
                true
        );
    }

    public static <T> T getOrInstantiate(final ApplicationContext ctx,
                                         final Class<? extends T> clazz) {
        try {
            return ctx.getBean(clazz);
        } catch (final BeansException e) {
            return instantiate(ctx, clazz);
        }
    }
}
