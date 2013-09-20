package org.nohope.spring.app;

import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/23/12 10:05 PM
 */
public final class ModuleDescriptor<ModuleType> {
    private final Properties properties;
    private final ModuleType module;
    private final String name;
    private final ConfigurableApplicationContext ctx;

    public ModuleDescriptor(@Nonnull final String name,
                            @Nonnull final ModuleType module,
                            @Nonnull final Properties properties,
                            @Nonnull final ConfigurableApplicationContext ctx) {
        this.properties = properties;
        this.module = module;
        this.name = name;
        this.ctx = ctx;
    }

    @Nonnull
    public ConfigurableApplicationContext getContext() {
        return ctx;
    }

    @Nonnull
    public Properties getProperties() {
        return properties;
    }

    @Nonnull
    public ModuleType getModule() {
        return module;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + "@" + module.getClass().getCanonicalName();
    }
}
