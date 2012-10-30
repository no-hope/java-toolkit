package org.nohope.spring.app;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.nohope.spring.SpringUtils;
import org.nohope.spring.app.module.IModule;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 5:29 PM
 */
public class SpringAsyncModularAppTest {

    public static class BeanWithId {
        private final String value;

        @Inject
        BeanWithId(@Named("beanWithId") final String value) {
            this.value = value;
        }
    }

    public static class BeanWithoutId {
        private final String value;

        @Inject
        BeanWithoutId(@Named("beanWithoutId") final String value) {
            this.value = value;
        }
    }

    public static class BeanWithIdAndName {
        private final String value;

        @Inject
        BeanWithIdAndName(@Named("beanWithIdAndName") final String value) {
            this.value = value;
        }
    }

    @Test
    public void generalInjectionTest() throws Exception {
        final AppWithContainer app = probe("app", "", "legalModuleDefaultContext");

        final ConfigurableApplicationContext context = app.getAppContext();
        assertEquals("beanWithId", context.getBean("beanWithId", String.class));
        assertEquals("beanWithoutId", context.getBean("beanWithoutId", String.class));
        assertEquals("beanWithIdAndName", context.getBean("beanWithIdAndName", String.class));
        assertEquals("mixedBean", context.getBean("beanId", String.class));
        assertEquals("mixedBean", context.getBean("beanName", String.class));

        assertEquals("beanWithId", SpringUtils.instantiate(context, BeanWithId.class).value);
        assertEquals("beanWithoutId", SpringUtils.instantiate(context, BeanWithoutId.class).value);
        assertEquals("beanWithIdAndName", SpringUtils.instantiate(context, BeanWithIdAndName.class).value);
    }

    @Test
    public void settersInvocation() throws Exception {
        final AppWithSetters app = probe(AppWithSetters.class, "app", "", "legalModuleDefaultContext");
        assertEquals("appBean", app.getAppBean());
    }

    @Test
    public void appDefaultContextOverriding() throws Exception {
        final AppWithContainer app = probe("appo", "appContextOverriding", "appContextOverriding");
        assertNotNull(app.getContext());
        assertEquals("appBeanOverridden", app.getContext().getBean("appBean"));
    }

    @Test
    public void moduleDefaultContextOverriding() throws Exception {
        final AppWithContainer app = probe("app", "", "moduleContextOverriding");

        assertEquals(1, app.getModules().size());
        final InjectModuleWithContextValue m = getModule(app, 0);
        assertEquals("overridden", m.getValue());
        assertEquals("moduleo", m.getName());
        assertEquals("appBean", m.getContext().getBean("appBean"));
    }

    @Test
    public void searchPathsDetermining() throws Exception {
        final SpringAsyncModularApp<IModule, AppWithContainer> app =
                new SpringAsyncModularApp<>(IModule.class, AppWithContainer.class);

        assertEquals("appWithContainer", app.getAppName());
        assertEquals("org.nohope.spring.app/", app.getAppMetaInfNamespace());
        assertEquals("org.nohope.spring.app/module/", app.getModuleMetaInfNamespace());
        assertEquals(IModule.class, app.getTargetModuleClass());
    }

    @Test
    public void concatTest() {
        assertEquals("test1" + File.separator + "test2" + File.separator +"test3" ,
                SpringAsyncModularApp.concat("test1", "test2/", "/test3"));
    }

    @Test
    public void illegalModuleDescriptor() throws Exception {
        final AppWithContainer app = probe("app", "", "illegalDescriptor");
        assertEquals(0, app.getModules().size());
    }

    @Test
    public void nonexistentModuleClass() throws Exception {
        final AppWithContainer app = probe("app", "", "nonexistentClass");
        assertEquals(0, app.getModules().size());
    }

    @Test
    public void notAModuleClass() throws Exception {
        final AppWithContainer app = probe("app", "", "notAModule");
        assertEquals(0, app.getModules().size());
    }

    /* all beans in contexts should be constructed ONCE! */
    @Test
    public void multipleConstructing() throws Exception {
        final AppWithContainer app = probe("app", "once", "once/module");

        assertEquals(2, app.getContext().getBean(OnceConstructable.class).getId());
    }

    @Test
    public void legalModuleDefaultContext() throws Exception {
        final AppWithContainer app = probe("app", "", "legalModuleDefaultContext");

        assertEquals(1, app.getModules().size());
        final InjectModuleWithContextValue m = getModule(app, 0);
        assertEquals("123", m.getValue());
        assertEquals("legal", m.getName());
        final Properties p = m.getProperties();
        assertEquals(2, p.size());
        assertEquals("\"fuck yeah!\"", p.getProperty("property"));

        // check for app beans inheritance
        assertEquals("appBean", m.getContext().getBean("appBean"));
    }

    public static class UtilsBean {
        // http://stackoverflow.com/a/1363435
        @Resource(name = "testList")
        private final List<String> list = new ArrayList<>();

        public List<String> getList() {
            return list;
        }
    }

    @Test
    public void utilsSupport() throws InterruptedException {
        final AppWithContainer app =  probe("utils",
                "utils",
                "legalModuleDefaultContext");
        assertNotNull(app.getContext().getBean("testList", List.class));
        final UtilsBean bean = SpringUtils.getOrInstantiate(app.getContext(), UtilsBean.class);

        final List<String> list = bean.getList();
        assertNotNull(list);
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
    }

    @SuppressWarnings("unchecked")
    private static <T extends IModule> T getModule(final AppWithContainer app,
                                                   final int index) {
        assertTrue(app.getModules().size() >= index);
        final IModule module = app.getModules().get(index);
        assertNotNull(module);
        try {
            return (T) module;
        } catch (ClassCastException e) {
            fail();
            return null;
        }
    }

    private static<T extends Handler<IModule>> T probe(final Class<T> clazz,
                                                               final String appName,
                                                               final String appMetaInfNamespace,
                                                               final String metaInfNamespace)
            throws InterruptedException {
        final SpringAsyncModularApp<IModule, T> app = new SpringAsyncModularApp<>(
                IModule.class,
                clazz,
                appName,
                appMetaInfNamespace,
                metaInfNamespace
        );

        final AtomicReference<Throwable> ref = new AtomicReference<>();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    app.start();
                } catch (final Exception e) {
                    ref.set(e);
                }
            }
        });

        t.start();
        app.stop();
        t.join();

        final Throwable throwable = ref.get();
        if (throwable != null) {
            throw new IllegalStateException(throwable);
        }

        //noinspection unchecked
        return app.getHandler();
    }

    private static AppWithContainer probe(final String appName,
                                          final String appMetaInfNamespace,
                                          final String metaInfNamespace)
            throws InterruptedException {
        return probe(AppWithContainer.class, appName, appMetaInfNamespace, metaInfNamespace);
    }
}
