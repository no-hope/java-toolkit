package org.nohope.spring.app;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.nohope.reflection.TypeReference;
import org.nohope.spring.BeanDefinition;
import org.nohope.spring.SpringUtils;

import javax.annotation.Nonnull;
import java.util.Properties;

import static org.nohope.spring.SpringUtils.instantiate;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/17/12 11:16 PM
 */
public abstract class Handler<M> {
    private ConfigurableApplicationContext ctx;
    private String appName;


    protected void onApplicationStop() {
    }

    /**
     * This method executed when app successfully found module.
     * Here is a good place to pass additional beans to module context
     * or process custom annotations of module class/class methods.
     *
     * @param ctx        final module context
     * @param clazz      module class
     * @param properties module descriptor
     * @param name       module name
     */
    protected void onModuleDiscovered(@Nonnull final Class<? extends M> clazz,
                                      @Nonnull final ConfigurableApplicationContext ctx,
                                      @Nonnull final Properties properties,
                                      @Nonnull final String name) {
    }

    /**
     * This method executed when app successfully instantiated module using it's context.
     * Here is a good place to pass do some module postprocessing.
     *
     * @param module     module class
     * @param ctx        final module context
     * @param properties module descriptor
     * @param name       module name
     */
    protected void onModuleCreated(@Nonnull final M module,
                                   @Nonnull final ConfigurableApplicationContext ctx,
                                   @Nonnull final Properties properties,
                                   @Nonnull final String name) {
    }

    /**
     * This method executed when all modules are successfully instantiated and processed.
     */
    protected void onModuleDiscoveryFinished() throws Exception {
    }

    /**
     * Retrieves bean from application context or instantiates it using autowiring rules.
     *
     * @param clazz target bean class
     * @param <T> bean type
     * @return created bean
     */
    @Nonnull
    protected final <T> T getOrInstantiate(@Nonnull final Class<? extends T> clazz) {
        try {
            return getAppContext().getBean(clazz);
        } catch (final BeansException e) {
            return instantiate(getAppContext(), clazz);
        }
    }

    @Nonnull
    protected final <T> T get(@Nonnull final String beanId, @Nonnull final Class<T> clazz) {
        return getAppContext().getBean(beanId, clazz);
    }

    protected final <T> T get(@Nonnull final String beanId, @Nonnull final TypeReference<T> reference) {
        return get(beanId, reference.getTypeClass());
    }

    protected static <T> T get(@Nonnull final ApplicationContext ctx,
                               @Nonnull final String beanId,
                               @Nonnull final TypeReference<T> reference) {
        return ctx.getBean(beanId, reference.getTypeClass());
    }

    protected static <T> T get(@Nonnull final ApplicationContext ctx,
                               @Nonnull final String beanId,
                               @Nonnull final Class<T> clazz) {
        return ctx.getBean(beanId, clazz);
    }

    @Nonnull
    protected final <T> T get(@Nonnull final Class<T> clazz) {
        return getAppContext().getBean(clazz);
    }

    @Nonnull
    protected <T> T get(@Nonnull final TypeReference<T> reference) {
        return get(reference.getTypeClass());
    }

    @Nonnull
    protected final <T> T get(@Nonnull final BeanDefinition<T> definition) {
        return get(definition.getName(), definition.getBeanClass());
    }

    @Nonnull
    protected <T> T registerSingleton(@Nonnull final String name, @Nonnull final T obj) {
        registerSingleton(getAppContext(), name, obj);
        return obj;
    }

    @Nonnull
    protected static <T> T registerSingleton(@Nonnull final ConfigurableApplicationContext ctx,
                                             @Nonnull final String name,
                                             @Nonnull final T obj) {
        return SpringUtils.registerSingleton(ctx, name, obj);
    }

    @Nonnull
    protected static <T> T getOrInstantiate(@Nonnull final ApplicationContext ctx,
                                            @Nonnull final Class<? extends T> clazz) {
        return SpringUtils.getOrInstantiate(ctx, clazz);
    }

    /**
     * <b>NOTE</b>: application context will be available only after
     * handler constructor will be invoked.
     *
     * @return application context
     */
    @Nonnull
    public final ConfigurableApplicationContext getAppContext() {
        ensureContext();
        return ctx;
    }

    final void setAppContext(final ConfigurableApplicationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * <b>NOTE</b>: application name will be available only after
     * handler constructor will be invoked.
     *
     * @return application context
     */
    @Nonnull
    public final String getAppName() {
        ensureContext();
        return appName;
    }

    final void setAppName(final String appName) {
        this.appName = appName;
    }

    private void ensureContext() {
        if (ctx == null) {
            throw new IllegalStateException("application context not yet available");
        }
    }
}
