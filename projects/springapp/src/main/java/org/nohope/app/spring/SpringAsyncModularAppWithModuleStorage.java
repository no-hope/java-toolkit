package org.nohope.app.spring;

import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Date: 30.07.12
 * Time: 17:18
 */
public class SpringAsyncModularAppWithModuleStorage<M> extends SpringAsyncModularApp<M> {
    private final Map<String, ModuleDescriptor<M>> modules = new HashMap<>();

    /**
     *
     * @return Map of descriptors of all the available modules
     */
    public Map<String, ModuleDescriptor<M>> getModules() {
        return modules;
    }

    /**
     *
     * @deprecated You should avoid of using this method if possible
     *
     * @param clazz
     * @param <Subtype> supertype of modules to be filtered
     * @return All the implementations of given superclass
     */
    public <Subtype> List<Subtype> getImplementations(final Class<Subtype> clazz) {
        return getImplementations(clazz, modules.values());
    }

    static <Subtype, MType> List<Subtype> getImplementations(final Class<Subtype> clazz, final Collection<ModuleDescriptor<MType>> source) {
        final List<Subtype> ret = new ArrayList<>();
        for (final ModuleDescriptor<MType> obj : source) {
            if (clazz.isAssignableFrom(obj.getModule().getClass())) {
                @SuppressWarnings("unchecked")
                final Subtype val = (Subtype) obj.getModule();
                ret.add(val);
            }

        }
        return ret;
    }


    protected SpringAsyncModularAppWithModuleStorage(@Nonnull final Class<? extends M> targetModuleClass, @Nullable final String appName, @Nullable final String appMetaInfNamespace, @Nullable final String moduleMetaInfNamespace) {
        super(targetModuleClass, appName, appMetaInfNamespace, moduleMetaInfNamespace);
    }

    protected SpringAsyncModularAppWithModuleStorage(final Class<? extends M> targetModuleClass) {
        super(targetModuleClass);
    }

    @Override
    protected void onModuleCreated(final M module,
                                   final ConfigurableApplicationContext ctx,
                                   final Properties properties,
                                   final String name) {
        modules.put(name, new ModuleDescriptor<>(name, module, properties));
    }
}
