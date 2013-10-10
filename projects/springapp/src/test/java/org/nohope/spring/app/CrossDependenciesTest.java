package org.nohope.spring.app;

import org.junit.Test;
import org.nohope.reflection.TypeReference;
import org.nohope.spring.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
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
            this.param2 = param2;
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

    public static class InvalidProviderType implements Module {
        @Inject
        public InvalidProviderType(final IDependencyProvider<?> param) {
        }
    }

    public static class InvalidProviderType2 implements Module {
        @Inject
        public InvalidProviderType2(final IDependencyProvider param) {
        }
    }

    public static class MultiplyConstructors implements Module {
        @Inject
        public MultiplyConstructors(final IDependencyProvider<KindA> param,
                                    final IDependencyProvider<KindB> param2) {
        }

        @Inject
        public MultiplyConstructors(final IDependencyProvider<KindB> param) {
        }
    }

    public static class InvalidDependencyAnnotation implements KindC {
        @Inject
        public InvalidDependencyAnnotation(@Dependency(KindB.class) final IDependencyProvider<KindA> param) {
        }
    }

    public static class InvalidDependencyAnnotation2 implements KindC {
        @Inject
        public InvalidDependencyAnnotation2(@Dependency(KindB.class) final IDependencyProvider<KindA> param1,
                                            @Dependency(KindA.class) final IDependencyProvider<KindB> param2) {
        }
    }

    @Test
    public void illegalDependencyProviderTypeParameter() {
        try {
            getResolutionOrder(Arrays.<Class<? extends Module>> asList(InvalidProviderType.class));
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Missing type information for dependency provider of "
                         + "org.nohope.spring.app.CrossDependenciesTest.InvalidProviderType "
                         + "module", e.getMessage());
        }
    }

    @Test
    public void illegalDependencyProviderTypeParameter2() {
        try {
            getResolutionOrder(Arrays.<Class<? extends Module>> asList(InvalidProviderType2.class));
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Unsupported type information found for dependency provider of "
                         + "org.nohope.spring.app.CrossDependenciesTest.InvalidProviderType2 "
                         + "module (no type specified?)", e.getMessage());
        }
    }

    @Test
    public void dependencyWithMultiplyInjectableConstructors() {
        try {
            getResolutionOrder(Arrays.asList(
                    TestClassA.class,
                    TestClassC.class,
                    MultiplyConstructors.class));
            fail();
        } catch (IllegalStateException e) {
            assertEquals("More than one injectable constructor found for class "
                         + "org.nohope.spring.app.CrossDependenciesTest$MultiplyConstructors", e.getMessage());
        }
    }

    @Test
    public void invalidDependencyAnnotationValue() {
        try {
            getResolutionOrder(Arrays.asList(
                    TestClassA.class,
                    TestClassC.class,
                    InvalidDependencyAnnotation.class));
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Parameter 0 of public org.nohope.spring.app.CrossDependenciesTest$"
                         + "InvalidDependencyAnnotation(org.nohope.spring.app.IDependencyProvider) "
                         + "must be annotated with @Dependency(KindA.class)", e.getMessage());
        }
    }

    @Test
    public void invalidDependencyAnnotationValue2() {
        try {
            getResolutionOrder(Arrays.asList(
                    TestClassA.class,
                    TestClassC.class,
                    InvalidDependencyAnnotation2.class));
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Parameter 0 of public org.nohope.spring.app.CrossDependenciesTest$"
                         + "InvalidDependencyAnnotation2(org.nohope.spring.app.IDependencyProvider,"
                         + "org.nohope.spring.app.IDependencyProvider) "
                         + "must be annotated with @Dependency(KindA.class)", e.getMessage());
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

    private static class TestSingleton {
    }

    private static class TestSingleton2 {
    }

    private static class CrossdepsHandler extends HandlerWithStorage<Module> {

        @Override
        protected void onModuleDiscoveryFinished() throws Exception {
            assertNotNull(getAppContext());
            assertNotNull(getOrInstantiate(getAppContext(), TestSingleton.class));
            assertNotNull(getAppName());

            final TestSingleton singleton = new TestSingleton();
            assertSame(singleton, registerSingleton("test", singleton));
            assertSame(singleton, getOrInstantiate(TestSingleton.class));
            assertSame(singleton, get(TestSingleton.class));
            assertSame(singleton, get(new TypeReference<TestSingleton>() {}));
            assertSame(singleton, get(BeanDefinition.of("test", TestSingleton.class)));
            assertSame(singleton, get("test", TestSingleton.class));
            assertSame(singleton, get("test", new TypeReference<TestSingleton>() {}));
            assertSame(singleton, get(getAppContext(), "test", TestSingleton.class));
            assertSame(singleton, get(getAppContext(), "test", new TypeReference<TestSingleton>() {}));

            final TestSingleton2 singleton2 = getOrInstantiate(TestSingleton2.class);
            assertNotNull(singleton2);

            assertSame(singleton2, registerSingleton("test2", singleton2));
            assertSame(singleton2, getOrInstantiate(TestSingleton2.class));
            assertSame(singleton2, get(TestSingleton2.class));
            assertSame(singleton2, get(new TypeReference<TestSingleton2>() {}));

            assertEquals(5, getModuleDescriptors().size());
            assertNotNull(getDescriptors(KindA.class));
            assertNotNull(getModules(KindA.class));
            assertNotNull(getModule(TestClassA.class, "a"));
            assertNotNull(getModule(KindA.class, "a"));
            assertNotNull(getImplementations(KindA.class));

            try {
                getModule(KindB.class, "a");
                fail();
            } catch (final IllegalArgumentException ignored) {
            }
        }

        @Override
        protected void onModuleDiscovered(
                @Nonnull final Class<? extends Module> clazz,
                @Nonnull final ConfigurableApplicationContext ctx,
                @Nonnull final Properties properties, @Nonnull final String name) {
            super.onModuleDiscovered(clazz, ctx, properties, name);    //To change body of overridden methods use File | Settings | File Templates.
        }
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
        try {
            handler.onModuleDiscoveryFinished();
        } catch (final Exception e) {
            fail();
        }
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
