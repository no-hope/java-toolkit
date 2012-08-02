package org.nohope.app.spring;

import org.springframework.context.ConfigurableApplicationContext;
import org.nohope.app.spring.module.IModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 5:33 PM
 */
class AppWithContainer extends SpringAsyncModularApp<IModule> {
    private ConfigurableApplicationContext context = null;

    AppWithContainer(final String appName,
                     final String metaInfNamespace) {
        super(IModule.class, appName, metaInfNamespace, metaInfNamespace);
    }

    AppWithContainer() {
        super(IModule.class);
    }

    AppWithContainer(final String appName,
                     final String appMetaInfNamespace,
                     final String metaInfNamespace) {
        super(IModule.class, appName, appMetaInfNamespace, metaInfNamespace);
    }

    private final List<IModule> modules = new ArrayList<>();

    @Override
    protected void onModuleCreated(final IModule module,
                                   final ConfigurableApplicationContext ctx,
                                   final Properties properties,
                                   final String name) {
        super.onModuleCreated(module, ctx, properties, name);
        modules.add(module);
    }

    @Override
    protected void onModuleDiscovered(final Class<? extends IModule> clazz,
                                      final ConfigurableApplicationContext ctx,
                                      final Properties properties,
                                      final String name) {
        super.onModuleDiscovered(clazz, ctx, properties, name);
        ctx.getBeanFactory().registerSingleton("name", name);
        ctx.getBeanFactory().registerSingleton("properties", properties);
        ctx.getBeanFactory().registerSingleton("ctx", ctx);
    }

    @Override
    protected void onModuleDiscoveryFinished(final ConfigurableApplicationContext ctx) throws Exception {
        super.onModuleDiscoveryFinished(ctx);
        context = ctx;
    }

    public ConfigurableApplicationContext getContext() {
        return context;
    }

    public List<IModule> getModules() {
        return modules;
    }
}
