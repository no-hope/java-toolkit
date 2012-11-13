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
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
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
 *          {@link SpringActorFactory#create() create}(ctx, MyActor.class)
 *              .addBean("bean", new Bean()) // additional @Named inject
 *              .addBean(new OtherBean())    // additional typed inject
 *              .getProps(),
 *          "myActor");
 * </pre>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 11:09 PM
 */
public final class SpringActorFactory<T extends UntypedActor> implements UntypedActorFactory {
    private static final long serialVersionUID = 0L;

    private final Class<T> clazz;
    private final transient List<Object> beans = new ArrayList<>();
    private final transient Map<String, Object> namedBeans = new HashMap<>();
    private final transient ApplicationContext ctx;

    public SpringActorFactory(@Nonnull final ApplicationContext ctx,
                              @Nonnull final Class<T> clazz) {
        this(ctx, clazz, null, null);
    }

    public static<T extends UntypedActor> SpringActorFactory<T> create(
            @Nonnull final ApplicationContext ctx,
            @Nonnull final Class<T> clazz) {
        return new SpringActorFactory<>(ctx, clazz);
    }

    public SpringActorFactory(@Nonnull final ApplicationContext ctx,
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

    public SpringActorFactory<T> addBeans(@Nonnull final Object... beans) {
        final List<Object> list = Arrays.asList(beans);
        if (list.contains(null)) {
            throw new IllegalStateException("null reference not allowed in beans list");
        }

        this.beans.addAll(list);
        return this;
    }

    public SpringActorFactory<T> addBean(final String name, @Nonnull final Object bean) {
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

    @Nonnull
    public Class<T> getTargetClass() {
        return clazz;
    }

    @Nonnull
    public List<Object> getBeans() {
        return beans;
    }

    @Nonnull
    public Map<String, Object> getNamedBeans() {
        return namedBeans;
    }

    @Nonnull
    public ApplicationContext getContext() {
        return ctx;
    }

    public Props getProps() {
        return new Props(this);
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void writeObject(final ObjectOutputStream oos) throws IOException {
        throw new NotSerializableException();
    }
}
