package org.nohope.spring.app;

import org.springframework.context.ConfigurableApplicationContext;
import org.nohope.reflection.TypeReference;

import javax.annotation.Nonnull;
import java.util.*;

import static org.nohope.reflection.IntrospectionUtils.cast;
import static org.nohope.reflection.IntrospectionUtils.instanceOf;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/17/12 11:16 PM
 */
public abstract class HandlerWithStorage<M> extends Handler<M> {
    private final Map<String, ModuleDescriptor<M>> modules = new HashMap<>();

    /**
     * @return Map of descriptors of all the available modules
     */
    @Nonnull
    public Map<String, ModuleDescriptor<M>> getModuleDescriptors() {
        return new TreeMap<>(modules);
    }

    protected <S> List<ModuleDescriptor<S>> getDescriptors(final Class<S> clazz) {
        return getDescriptors(clazz, modules.values());
    }

    protected <S> Map<String, S> getModules(final Class<S> clazz) {
        final Map<String, S> result = new HashMap<>();
        for (final ModuleDescriptor<S> d: getDescriptors(clazz, modules.values())) {
            result.put(d.getName(), d.getModule());
        }
        return result;
    }

    /**
     * @param clazz
     * @param <S> supertype of modules to be filtered
     * @return All the implementations of given superclass
     */
    protected <S> List<S> getImplementations(final Class<S> clazz) {
        return getImplementations(clazz, modules.values());
    }

    static <M, S> List<ModuleDescriptor<S>> getDescriptors(
            final Class<S> clazz,
            final Collection<ModuleDescriptor<M>> source) {

        final List<ModuleDescriptor<S>> ret = new ArrayList<>();
        for (final ModuleDescriptor<M> obj : source) {
            final M module = obj.getModule();
            if (instanceOf(module.getClass(), clazz)) {
                ret.add(cast(obj, new TypeReference<ModuleDescriptor<S>>() {}));
            }
        }
        return ret;
    }


    static <S, M> List<S> getImplementations(final Class<S> clazz,
                                             final Collection<ModuleDescriptor<M>> source) {
        final List<S> ret = new ArrayList<>();
        for (final ModuleDescriptor<M> obj : source) {
            final M module = obj.getModule();
            if (instanceOf(module.getClass(), clazz)) {
                ret.add(cast(module, clazz));
            }
        }
        return ret;
    }

    protected M getModule(final String moduleName) {
        return modules.get(moduleName).getModule();
    }

    protected <Subtype extends M> Subtype getModule(final Class<Subtype> clazz,
                                                    final String moduleName) {
        final ModuleDescriptor<M> md = modules.get(moduleName);
        if (clazz.isAssignableFrom(md.getClass())) {
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            final Subtype module = (Subtype) md.getModule();
            return module;
        }
        throw new IllegalArgumentException("No module '"+moduleName+"' with given type "+clazz.getCanonicalName());
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
    @Override
    protected final void onModuleCreated(@Nonnull final M module,
                                         @Nonnull final ConfigurableApplicationContext ctx,
                                         @Nonnull final Properties properties,
                                         @Nonnull final String name) {
        final ModuleDescriptor<M> descriptor = new ModuleDescriptor<>(name, module, properties, ctx);
        modules.put(name, descriptor);
        onModuleAdded(descriptor);
    }

    protected void onModuleAdded(@Nonnull final ModuleDescriptor<M> descriptor) {
    }
}
