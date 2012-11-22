package org.nohope.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

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
    @Nullable
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

    @Nonnull
    public static ConfigurableApplicationContext propagateAnnotationProcessing(
            @Nonnull final ConfigurableApplicationContext ctx) {
        final ConfigurableListableBeanFactory factory = ctx.getBeanFactory();
        {
            final CommonAnnotationBeanPostProcessor processor = new CommonAnnotationBeanPostProcessor();
            processor.setBeanFactory(factory);
            factory.addBeanPostProcessor(processor);
        }
        {
            final AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
            processor.setBeanFactory(factory);
            factory.addBeanPostProcessor(processor);
        }
        return ctx;
    }

    @Nonnull
    public static <T> T registerSingleton(@Nonnull final ConfigurableApplicationContext ctx,
                                          @Nonnull final String name,
                                          @Nonnull final T obj,
                                          @Nonnull final Class<? extends Annotation> annotation,
                                          @Nonnull final Object value) {
        final RootBeanDefinition beanDef = new RootBeanDefinition(obj.getClass());
        beanDef.setScope(AbstractBeanDefinition.SCOPE_SINGLETON);
        beanDef.setAutowireCandidate(true);
        beanDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO);
        beanDef.addQualifier(new AutowireCandidateQualifier(annotation, value));


        ((BeanDefinitionRegistry) ctx.getBeanFactory())
                .registerBeanDefinition(name, beanDef);
        ctx.getBeanFactory().registerSingleton(name, obj);
        return obj;
    }

    @Nonnull
    public static <T> T registerSingleton(@Nonnull final ConfigurableApplicationContext ctx,
                                          @Nonnull final String name,
                                          @Nonnull final T obj) {
        final RootBeanDefinition beanDef = new RootBeanDefinition(obj.getClass());
        beanDef.setScope(AbstractBeanDefinition.SCOPE_SINGLETON);
        beanDef.setAutowireCandidate(true);
        beanDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO);

        ((BeanDefinitionRegistry) ctx.getBeanFactory())
                .registerBeanDefinition(name, beanDef);
        ctx.getBeanFactory().registerSingleton(name, obj);

        return obj;
    }

    @Nonnull
    public static <T> T registerSingleton(@Nonnull final ConfigurableApplicationContext ctx,
                                          @Nonnull final T obj) {
        ctx.getBeanFactory().registerSingleton(obj.getClass().getCanonicalName(), obj);
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(@Nonnull final ApplicationContext ctx,
                                    @Nonnull final Class<? extends T> clazz) {
        final AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
        return (T) factory.createBean(
                clazz,
                AutowireCapableBeanFactory.AUTOWIRE_NO,
                true
        );
    }

    public static void setProperties(@Nonnull final ApplicationContext ctx,
                                     @Nonnull final Object bean) {
        final AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
        factory.autowireBeanProperties(
                bean,
                AutowireCapableBeanFactory.AUTOWIRE_NO,
                true
        );
    }

    @Nonnull
    public static <T> T getOrInstantiate(@Nonnull final ApplicationContext ctx,
                                         @Nonnull final Class<? extends T> clazz) {
        try {
            return ctx.getBean(clazz);
        } catch (final BeansException e) {
            return instantiate(ctx, clazz);
        }
    }
}
