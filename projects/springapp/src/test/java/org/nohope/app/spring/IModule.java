package org.nohope.app.spring;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 6:09 PM
 */
public interface IModule {
    String getName();

    Properties getProperties();

    ConfigurableApplicationContext getContext();
}
