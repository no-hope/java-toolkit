package org.nohope.spring.app;

import org.springframework.context.ConfigurableApplicationContext;

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
    public Map<String, ModuleDescriptor<M>> getModules() {
        return modules;
    }

    /**
     * @param clazz
     * @param <Subtype> supertype of modules to be filtered
     * @return All the implementations of given superclass
     */
    protected <Subtype> List<Subtype> getImplementations(final Class<Subtype> clazz) {
        return getImplementations(clazz, modules.values());
    }

    static <Subtype, MType> List<Subtype> getImplementations(final Class<Subtype> clazz, final Collection<ModuleDescriptor<MType>> source) {
        final List<Subtype> ret = new ArrayList<>();
        for (final ModuleDescriptor<MType> obj : source) {
            final MType module = obj.getModule();
            if (instanceOf(module.getClass(), clazz)) {
                ret.add(cast(module, clazz));
            }
        }
        return ret;
    }

    protected M getModule(final String moduleName) {
        return modules.get(moduleName).getModule();
    }

    protected <Subtype extends M> Subtype getModule(final Class<Subtype> clazz, final String moduleName) {
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
        modules.put(name, new ModuleDescriptor<>(name, module, properties));
        onModuleAdded(module, ctx, properties, name);
    }

    protected final void onModuleAdded(@Nonnull final M module,
                                       @Nonnull final ConfigurableApplicationContext ctx,
                                       @Nonnull final Properties properties,
                                       @Nonnull final String name) {
    }
}
