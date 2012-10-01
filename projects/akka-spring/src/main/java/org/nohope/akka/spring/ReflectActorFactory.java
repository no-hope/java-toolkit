package org.nohope.akka.spring;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.nohope.spring.SpringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nohope.spring.SpringUtils.instantiate;
import static org.nohope.spring.SpringUtils.registerSingleton;

/**
 * This class is used for spring-driven actor creation. Allows to inject
 * bean dependencies in actor and add external inject dependencies which
 * can be not defined in given context.
 * <p/>
 * <b>Typical usage</b>:
 * <pre>
 *     class MyActor extends {@link akka.actor.UntypedActor UntypedActor} {
 *          &#064;{@link javax.inject.Inject Inject}
 *          MyActor (&#064;{@link javax.inject.Named Named}(name="stringFromContext") String param,
 *                   GlobalBean beanFromContext,
 *                   &#064;{@link javax.inject.Named Named}(name="bean") Bean bean,
 *                   OtherBean otherBean) {
 *              ...
 *          }
 *     }
 *
 *     {@link org.springframework.context.ApplicationContext ApplicationContext} ctx;
 *     {@link akka.actor.ActorSystem ActorSystem} system;
 *
 *     {@link akka.actor.ActorRef ActorRef} ref = system.actorOf(
 *          {@link ReflectActorFactory#create() create}(ctx, MyActor.class)
 *              .addBean("bean", new Bean()) // additional @Named inject
 *              .addBean(new OtherBean())    // additional typed inject
 *              .getProps(),
 *          "myActor");
 * </pre>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 11:09 PM
 */
public class ReflectActorFactory<T extends UntypedActor> implements UntypedActorFactory {
    private static final long serialVersionUID = 0L;

    private final Class<T> clazz;
    private final transient List<Object> beans = new ArrayList<>();
    private final transient Map<String, Object> namedBeans = new HashMap<>();
    private final transient ApplicationContext ctx;

    public ReflectActorFactory(@Nonnull final ApplicationContext ctx,
                               @Nonnull final Class<T> clazz) {
        this(ctx, clazz, null, null);
    }

    public static<T extends UntypedActor> ReflectActorFactory<T> create(
            @Nonnull final ApplicationContext ctx,
            @Nonnull final Class<T> clazz) {
        return new ReflectActorFactory<>(ctx, clazz);
    }

    public ReflectActorFactory(@Nonnull final ApplicationContext ctx,
                               @Nonnull final Class<T> clazz,
                               @Nullable final List<Object> objects,
                               @Nullable final Map<String, Object> namedObjects) {
        this.clazz = clazz;
        this.ctx = ctx;

        if (objects != null) {
            beans.addAll(objects);
        }
        if (namedObjects != null) {
            namedBeans.putAll(namedObjects);
        }
    }

    public ReflectActorFactory<T> addBeans(final Object... beans) {
        this.beans.addAll(Arrays.asList(beans));
        return this;
    }

    public ReflectActorFactory<T> addBean(final String name, final Object bean) {
        this.namedBeans.put(name, bean);
        return this;
    }

    @Override
    public UntypedActor create() {
        final ConfigurableApplicationContext child =
                SpringUtils.propagateAnnotationProcessing(
                        new GenericApplicationContext(ctx));
        for (final Object obj : beans) {
            registerSingleton(child, obj);
        }
        for (final Map.Entry<String, Object> e : namedBeans.entrySet()) {
            registerSingleton(child, e.getKey(), e.getValue());
        }

        return instantiate(child, clazz);
    }

    public Class<T> getTragetClass() {
        return clazz;
    }

    public List<Object> getBeans() {
        return beans;
    }

    public Map<String, Object> getNamedBeans() {
        return namedBeans;
    }

    public ApplicationContext getContext() {
        return ctx;
    }

    public Props getProps() {
        return new Props(this);
    }
}
