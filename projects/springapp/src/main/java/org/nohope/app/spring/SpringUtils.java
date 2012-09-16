package org.nohope.app.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 10:40 PM
 */
public class SpringUtils {
    private SpringUtils() {
    }

    public static ConfigurableApplicationContext propagateAnnotationProcessing(
            final ConfigurableApplicationContext ctx) {
        ctx.getBeanFactory().addBeanPostProcessor(new CommonAnnotationBeanPostProcessor());
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
    public static <T> T instantiate(final ApplicationContext ctx, final Class<? extends T> clazz) {
        final AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
        return (T) factory.createBean(
                clazz,
                AutowireCapableBeanFactory.AUTOWIRE_NO,
                true
        );
    }

    public static <T> T getOrInstantiate(final ApplicationContext ctx, final Class<? extends T> clazz) {
        try {
            return ctx.getBean(clazz);
        } catch (final BeansException e) {
            return instantiate(ctx, clazz);
        }
    }
}
