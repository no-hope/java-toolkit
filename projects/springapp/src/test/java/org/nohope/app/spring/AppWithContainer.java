package org.nohope.app.spring;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 7/27/12 5:33 PM
*/
public abstract class AppWithContainer extends SpringAsyncModularApp<IModule> {
    protected AppWithContainer(final String appName,
                               final String metaInfNamespace) {
        super(IModule.class, appName, metaInfNamespace, metaInfNamespace);
    }

    protected AppWithContainer(final String appName,
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
        modules.add(module);
    }

    @Override
    protected void onModuleDiscovered(final Class<? extends IModule> clazz,
                                      final ConfigurableApplicationContext ctx,
                                      final Properties properties,
                                      final String name) {
        ctx.getBeanFactory().registerSingleton("name", name);
        ctx.getBeanFactory().registerSingleton("properties", properties);
        ctx.getBeanFactory().registerSingleton("ctx", ctx);
    }

    public List<IModule> getModules() {
        return modules;
    }
}
