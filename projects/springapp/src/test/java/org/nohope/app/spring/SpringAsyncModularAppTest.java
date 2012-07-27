package org.nohope.app.spring;

import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 5:29 PM
 */
public class SpringAsyncModularAppTest {

    @Test
    public void illegalModuleDescriptor() throws Exception {
        final AppWithContainer app = new AppWithContainer("app", "", "illegalDescriptor") {};
        start(app);
        app.stop();

        assertEquals(0, app.getModules().size());
    }

    @Test
    public void legalModuleDefaultContext() throws Exception {
        final AppWithContainer app = new AppWithContainer("app", "", "legalModuleDefaultContext") {};
        start(app);
        app.stop();

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

    private static void start(final AppWithContainer app) throws InterruptedException {
        final AtomicReference<Throwable> ref = new AtomicReference<Throwable>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    app.start();
                } catch (final Exception e) {
                    e.printStackTrace();
                    ref.set(e);
                }
            }
        }).start();

        while (!app.isStarted()) {
            TimeUnit.SECONDS.sleep(1);
        }

        if (ref.get() != null) {
            throw new IllegalStateException(ref.get());
        }
    }
}
