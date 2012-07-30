package org.nohope.app.spring;

import org.springframework.context.ConfigurableApplicationContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 5:34 PM
 */
final class InjectModuleWithContextValue extends InjectModule {

    private final String value;

    @Inject
    private InjectModuleWithContextValue(
            @Named("value") final String val,
            @Named("name") final String name,
            @Named("properties") final Properties properties,
            @Named("ctx") final ConfigurableApplicationContext context) {
        super(name, properties, context);
        this.value = val;
    }

    public String getValue() {
        return value;
    }
}
