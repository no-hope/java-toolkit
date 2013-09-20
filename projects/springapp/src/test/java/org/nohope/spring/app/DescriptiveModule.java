package org.nohope.spring.app;

import org.springframework.context.ConfigurableApplicationContext;
import org.nohope.spring.app.module.IModule;

import java.util.Properties;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 5:34 PM
 */
class DescriptiveModule implements IModule {
    private String name;
    private Properties properties;
    private ConfigurableApplicationContext context;

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public ConfigurableApplicationContext getContext() {
        return context;
    }

    public void setContext(final ConfigurableApplicationContext context) {
        this.context = context;
    }
}
