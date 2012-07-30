package org.nohope.app.spring;

import java.util.Properties;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/23/12 10:05 PM
 */
public final class ModuleDescriptor<ModuleType> {
    private final Properties properties;
    private final ModuleType module;
    private final String name;

    public ModuleDescriptor(final String name, final ModuleType module, final Properties properties) {
        this.properties = properties;
        this.module = module;
        this.name = name;
    }

    public Properties getProperties() {
        return properties;
    }

    public ModuleType getModule() {
        return module;
    }

    public String getName() {
        return name;
    }
}
