package org.nohope.spring.app;

import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.nohope.spring.app.SpringAsyncModularApp.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/22/12 5:29 PM
 */
@SuppressWarnings({"UnusedParameters", "SpringJavaAutowiringInspection"})
public class CrossDependenciesTest {
    public interface Module {}
    public interface KindA extends Module {}
    public interface KindB extends Module {}
    public interface KindC extends Module {}

    public static class TestClassA implements KindA {
    }

    public static class TestClassD implements KindA {
    }

    public static class TestClassB implements KindB {
        private final IDependencyProvider<KindA> param;

        @Inject
        public TestClassB(final IDependencyProvider<KindA> param) {
            this.param = param;
        }

        public IDependencyProvider<KindA> getParam() {
            return param;
        }
    }

    public static class TestClassC implements KindB {
        private final IDependencyProvider<KindA> param;

        @Inject
        public TestClassC(final IDependencyProvider<KindA> param) {
            this.param = param;
        }

        public IDependencyProvider<KindA> getParam() {
            return param;
        }
    }

    public static class TestClassE implements KindC {
        private final IDependencyProvider<KindA> param1;
        private final IDependencyProvider<KindB> param2;

        @Inject
        public TestClassE(@Dependency(KindA.class) final IDependencyProvider<KindA> param1,
                          @Dependency(KindB.class) final IDependencyProvider<KindB> param2) {
            this.param1 = param1;
            this.param2= param2;
        }

        public IDependencyProvider<KindA> getParam1() {
            return param1;
        }

        public IDependencyProvider<KindB> getParam2() {
            return param2;
        }
    }

    public static class CycleReference implements KindC {
        @Inject
        public CycleReference(final IDependencyProvider<KindC> param1) {
        }
    }

    public static class DuplicateDependency implements Module {
        @Inject
        public DuplicateDependency(final IDependencyProvider<KindA> param1,
                                   final IDependencyProvider<KindA> param2) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void cycleDependency() {
        getResolutionOrder(
                asList(TestClassB.class,
                        TestClassA.class,
                        TestClassC.class,
                        TestClassD.class,
                        TestClassE.class,
                        CycleReference.class));
    }

    @Test
    public void dependencyMatrix() {
        final Map<Class<? extends Module>, Set<Class<?>>> expected = new HashMap<>();
        expected.put(TestClassA.class, new HashSet<Class<?>>());
        expected.put(TestClassD.class, new HashSet<Class<?>>());
        expected.put(TestClassB.class, new HashSet<Class<?>>(asList(TestClassA.class, TestClassD.class)));
        expected.put(TestClassC.class, new HashSet<Class<?>>(asList(TestClassA.class, TestClassD.class)));
        expected.put(TestClassE.class, new HashSet<Class<?>>(asList(
                TestClassA.class, TestClassB.class, TestClassC.class, TestClassD.class)));

        assertEquals(expected, getDependencyMatrix(
                asList(TestClassB.class,
                       TestClassA.class,
                       TestClassC.class,
                       TestClassD.class,
                       TestClassE.class)));
    }

    @Test
    public void instantiationOrder() {
        final List<Class<? extends Module>> expected = new ArrayList<>();
        expected.addAll(asList(
                TestClassA.class,
                TestClassD.class,
                TestClassB.class,
                TestClassC.class,
                TestClassE.class));

        assertEquals(expected, getResolutionOrder(
                asList(TestClassB.class,
                        TestClassA.class,
                        TestClassC.class,
                        TestClassD.class,
                        TestClassE.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateDependency() {
        getDependencies(DuplicateDependency.class);
    }

    private static class CrossdepsHandler extends HandlerWithStorage<Module> {
    }

    @Test
    public void appTest() throws InterruptedException {
        final SpringAsyncModularApp<Module, CrossdepsHandler> app =
                new SpringAsyncModularApp<>(
                Module.class,
                CrossdepsHandler.class,
                "app",
                "crossdeps",
                "crossdeps/module"
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

        final CrossdepsHandler handler = app.getHandler();
        for (final ModuleDescriptor<Module> d : handler.getModuleDescriptors().values()) {
            final Module module = d.getModule();
            if (module instanceof TestClassE) {
                final TestClassE e = (TestClassE) module;
                assertEquals(toSet(asList(handler.getModule("d"), handler.getModule("a"))),
                             toSet(e.getParam1()));
                assertEquals(toSet(asList(handler.getModule("c"), handler.getModule("b"))),
                             toSet(e.getParam2()));
            } else if (module instanceof TestClassC) {
                final TestClassC c = (TestClassC) module;
                assertEquals(toSet(asList(handler.getModule("d"), handler.getModule("a"))),
                             toSet(c.getParam()));
            }
        }
    }

    public static<T> Set<T> toSet(final Iterable<T> i) {
        final Set<T> result = new HashSet<>();
        for (final T obj : i) {
            result.add(obj);
        }
        return result;
    }
}
