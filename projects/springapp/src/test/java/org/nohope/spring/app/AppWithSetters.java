package org.nohope.spring.app;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/16/12 11:50 PM
 */
public class AppWithSetters extends AppWithContainer {
    private String appBean;

    AppWithSetters(final String appName,
                   final String appMetaInfNamespace,
                   final String metaInfNamespace) {
        super(appName, appMetaInfNamespace, metaInfNamespace);
    }

    public String getAppBean() {
        return appBean;
    }

    @Inject
    public void setAppBean(@Named("appBean") final String appBean) {
        this.appBean = appBean;
    }
}
