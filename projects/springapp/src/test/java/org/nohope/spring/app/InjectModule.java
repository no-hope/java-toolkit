package org.nohope.spring.app;

import org.springframework.context.ConfigurableApplicationContext;
import org.nohope.spring.app.module.IModule;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 5:34 PM
 */
class InjectModule implements IModule {

    private final String name;
    private final Properties properties;
    private final ConfigurableApplicationContext context;

    @Inject
    public InjectModule(@Named("name") final String name,
                        @Named("properties") final Properties properties,
                        @Named("ctx") final ConfigurableApplicationContext context) {
        this.name = name;
        this.properties = properties;
        this.context = context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public ConfigurableApplicationContext getContext() {
        return context;
    }
}
