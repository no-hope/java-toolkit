package org.nohope.app.spring;

import org.apache.xbean.finder.ResourceFinder;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.nohope.app.AsyncApp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.nohope.app.spring.SpringUtils.instantiate;
import static org.nohope.app.spring.SpringUtils.propagateAnnotationProcessing;

/**
 * <b>Technical background</b>
 * <p/>
 * This class assumes following classpath hierarchy:
 * <pre>
 *     classpath:
 *          META-INF/[appMetaInfNamespace]/[appName]-defaultContext.xml // mandatory
 *          [appName]-context.xml // <-- optional, will override default service context
 *
 *          META-INF/[moduleMetaInfNamespace]/[moduleName1].properties // <-- module descriptor, mandatory
 *          META-INF/[moduleMetaInfNamespace]/[moduleName1]-defaultContext.xml // mandatory
 *          [moduleName1]-context.xml // <-- optional, will override default module context
 * </pre>
 * <p/>
 * Module descriptor is simple properties file content should looks like:
 * <pre>
 *     class=com.mypackage.ConcreteModule  // reserved
 *
 *     // any additional parameters
 *     key1=val1
 *     key2=val2
 * </pre>
 * <p/>
 * Module name retrieved from properties file name.
 * <p/>
 * <b>Start procedure</b>
 * <p/>
 * On {@link  #start() start} app will search for it's default service context, override it
 * with optionally passed service context. Then it searches for module definitions,
 * doing the same operations with their context (<b>note:</b> service context will be passed as a
 * parent to module context, so module will have access to all app beans) invoking appropriate
 * methods ({@link #onModuleDiscovered(Class, ConfigurableApplicationContext, Properties, String)
 * onModuleDiscovered},
 * {@link #onModuleCreated(Object, ConfigurableApplicationContext, Properties, String)
 * onModuleDiscovered}). After all
 * {@link #onModuleDiscoveryFinished(ConfigurableApplicationContext)
 * onModuleDiscoveryFinished}
 * will be executed.
 *
 * @param <M> module interface
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 3:31 PM
 */
public abstract class SpringAsyncModularApp<M> extends AsyncApp {
    private static final Logger LOG = LoggerFactory.getLogger(SpringAsyncModularApp.class);

    private static final String META_INF = "META-INF/";
    private static final String DEFAULT_MODULE_FOLDER = "module/";
    private static final String DEFAULT_CONTEXT_POSTFIX = "-defaultContext.xml";
    private static final String PROPERTIES_EXTENSION = ".properties";
    private static final String CONTEXT_POSTFIX = "-context.xml";

    private final ResourceFinder finder = new ResourceFinder(META_INF);

    private final AtomicReference<ConfigurableApplicationContext> contextReference = new AtomicReference<>();
    private final Class<? extends M> targetModuleClass;
    private final String appMetaInfNamespace;
    private final String moduleMetaInfNamespace;
    private final String appName;

    /**
     * {@link SpringAsyncModularApp} constructor.
     *
     * @param targetModuleClass      module type
     * @param appName                this app name (if {@code null} passed then class name with lowercase
     *                               first letter will be used)
     * @param appMetaInfNamespace    path relative to {@code META-INF} folder in classpath
     *                               to discover app context (if {@code null} passed then package path of app class will be used)
     * @param moduleMetaInfNamespace path relative to {@code META-INF} folder in classpath
     *                               to discover module context (if {@code null} passed then package path of targetClass
     *                               parameter will be used will be used)
     */
    protected SpringAsyncModularApp(@Nonnull final Class<? extends M> targetModuleClass,
                          @Nullable final String appName,
                          @Nullable final String appMetaInfNamespace,
                          @Nullable final String moduleMetaInfNamespace) {
        this.targetModuleClass = targetModuleClass;
        this.appMetaInfNamespace =
                appMetaInfNamespace == null
                        ? getPackage()
                        : toValidPath(appMetaInfNamespace);
        this.moduleMetaInfNamespace =
                moduleMetaInfNamespace == null
                        ? getPackage() + DEFAULT_MODULE_FOLDER
                        : toValidPath(moduleMetaInfNamespace);
        this.appName =
                appName == null
                        ? lowercaseClassName(getClass())
                        : appName;
    }

    /**
     * {@link SpringAsyncModularApp} constructor. Service name and search paths will
     * found reflectively (see {@link #SpringAsyncModularApp(Class, String, String, String)}).
     *
     * @param targetModuleClass module type
     */
    protected SpringAsyncModularApp(final Class<? extends M> targetModuleClass) {
        this(targetModuleClass, null, null, null);
    }

    /**
     * Searches for plugin definitions in classpath and instantiates them.
     */
    @Override
    protected final void onStart() throws Exception {
        final ConfigurableApplicationContext ctx = overrideRule(null, appMetaInfNamespace, appName);
        contextReference.set(ctx);

        final Map<String, Properties> properties = finder.mapAvailableProperties(moduleMetaInfNamespace);
        for (final Map.Entry<String, Properties> e : properties.entrySet()) {
            final String moduleFileName = e.getKey();
            final Properties moduleProperties = e.getValue();

            if (!moduleFileName.endsWith(PROPERTIES_EXTENSION)) {
                continue;
            }
            final String moduleName = moduleFileName.substring(0, moduleFileName.lastIndexOf('.'));

            /*
            TODO: in fact we'll never face with such problem ?
            I think we need to throw exception here. pshirshov.
            if (modules.containsKey(moduleName)) {
                LOG.warn("Module '{}' was already loaded, skipping", moduleName);
                continue;
            }
            */

            if (!moduleProperties.containsKey("class")) {
                LOG.warn("Unable to process module '{}' - no class property found", moduleName);
                continue;
            }

            final String moduleClassName = moduleProperties.getProperty("class");
            final Class<?> moduleClazz;
            try {
                moduleClazz = Class.forName(moduleClassName);
                if (!targetModuleClass.isAssignableFrom(moduleClazz)) {
                    LOG.error("Unable to load module '{}' class '{}' is not subclass of '{}'",
                            moduleName, moduleClazz.getCanonicalName(), targetModuleClass.getCanonicalName());
                    continue;
                }
            } catch (final ClassNotFoundException ex) {
                LOG.warn(ex, "Unable to load module '{}' class '{}' was not found",
                        moduleName, moduleClassName);
                continue;
            }

            final ConfigurableApplicationContext moduleContext = overrideRule(ctx,
                    moduleMetaInfNamespace, moduleName);

            final Class<? extends M> finalClass = moduleClazz.asSubclass(targetModuleClass);

            onModuleDiscovered(finalClass, moduleContext, moduleProperties, moduleName);

            final M module = instantiate(moduleContext, finalClass);
            LOG.debug("Module {}(class={}) loaded", moduleName, moduleClassName);

            onModuleCreated(module, moduleContext, moduleProperties, moduleName);
        }

        /*
        if (!finder.getResourcesNotLoaded().isEmpty()) {
            LOG.warn("Suspicious modules found in classpath: {}", finder.getResourcesNotLoaded());
        }
        */
        onModuleDiscoveryFinished(ctx);

        LOG.info("Service started: {}", this.getClass().getCanonicalName());
    }

    /**
     * This method executed when app successfully found module.
     * Here is a good place to pass additional beans to module context
     * or process custom annotations of module class/class methods.
     *
     * @param ctx        final module context
     * @param clazz      module class
     * @param properties module descriptor
     * @param name       module name
     */
    protected void onModuleDiscovered(final Class<? extends M> clazz,
                                      final ConfigurableApplicationContext ctx,
                                      final Properties properties,
                                      final String name) {
    }

    /**
     * This method executed when app successfully instantiated module using it's context.
     * Here is a good place to pass do some module postprocessing.
     *
     * @param module     module class
     * @param ctx        final module context
     * @param properties module descriptor
     * @param name       module name
     */
    void onModuleCreated(final M module,
                         final ConfigurableApplicationContext ctx,
                         final Properties properties,
                         final String name) {
    }

    /**
     * This method executed when all modules are successfully instantiated and processed.
     *
     * @param ctx final app context
     */
    protected void onModuleDiscoveryFinished(final ConfigurableApplicationContext ctx) throws Exception {
    }

    final Class<? extends M> getTargetModuleClass() {
        return targetModuleClass;
    }

    final String getAppMetaInfNamespace() {
        return appMetaInfNamespace;
    }

    final String getModuleMetaInfNamespace() {
        return moduleMetaInfNamespace;
    }

    final String getAppName() {
        return appName;
    }

    private String getPackage() {
        return getPackage(getClass());
    }

    /**
     * @return context of this application
     * @throws IllegalStateException if {@link #start()} wasn't called yet
     */
    final ConfigurableApplicationContext getAppContext() {
        final ConfigurableApplicationContext ctx = contextReference.get();
        if (ctx != null) {
            return ctx;
        }

        throw new IllegalStateException("Unable to find application context. App not started?");
    }

    /**
     * Retrieves bean from application context of instantiates it using
     * autowired rules instead.
     *
     * @param clazz target bean class
     * @param <T> bean type
     * @return created bean
     */
    protected <T> T getOrInstantiate(final Class<? extends T> clazz) {
        try {
            return getAppContext().getBean(clazz);
        } catch (final BeansException e) {
            return instantiate(getAppContext(), clazz);
        }
    }

    protected <T> T get(final String beanId, final Class<T> clazz) {
        return getAppContext().getBean(beanId, clazz);
    }

    protected <T> T get(final Class<T> clazz) {
        return getAppContext().getBean(clazz);
    }

    protected <T> T registerSingleton(final String name, final T obj) {
        registerSingleton(getAppContext(), name, obj);
        return obj;
    }

    protected static <T> T registerSingleton(final ConfigurableApplicationContext ctx,
                                             final String name,
                                             final T obj) {
        return SpringUtils.registerSingleton(ctx, name, obj);
    }

    private static ConfigurableApplicationContext overrideRule(@Nullable final ConfigurableApplicationContext ctx,
                                                               final String namespace,
                                                               final String name) {
        final String parentPath = concat(META_INF, namespace, name + DEFAULT_CONTEXT_POSTFIX);
        final String childPath = concat(namespace, name + CONTEXT_POSTFIX);

        if (!isResourceExists(parentPath)) {
            throw new IllegalArgumentException("Unable to create parent context for " + parentPath);
        }

        if (isResourceExists(childPath)) {
            return ensureCreate(ctx, parentPath, childPath);
        }

        LOG.warn("Unable to override {} with {}", parentPath, childPath);
        return ensureCreate(ctx, parentPath);
    }

    //TODO: move to utility classes?

    static String concat(final String... paths) {
        if (paths.length == 0) {
            return "";
        }

        File file = new File(paths[0]);
        for (int i = 1; i < paths.length ; i++) {
            file = new File(file, paths[i]);
        }

        return file.getPath();
    }

    private static String getPackage(final Class<?> clazz) {
        return toValidPath(clazz.getPackage().getName());
    }

    private static String lowercaseClassName(final Class<?> clazz) {
        if (clazz.isAnonymousClass()) {
            return lowercaseClassName(clazz.getEnclosingClass());
        }
        final String str = clazz.getSimpleName();
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    private static String toValidPath(final String str) {
        return str.endsWith("/") ? str : (str + '/');
    }

    private static boolean isResourceExists(final String path) {
        return new ClassPathResource(path).exists();
    }

    /**
     * Overrides given context with sequential list of contexts specified by classpath file names.
     *
     * @param parent spring context
     * @param paths contexts path
     * @return overridden context ({@code null} if one of given files not exists)
     */
    private static ConfigurableApplicationContext ensureCreate(@Nullable final ConfigurableApplicationContext parent,
                                                               final String... paths) {
        for (final String path: paths) {
            final ClassPathResource config = new ClassPathResource(path);
            if (!config.exists()) {
                return null;
            }
        }

        return propagateAnnotationProcessing(new ClassPathXmlApplicationContext(paths, parent));
    }

    protected static <T> T getOrInstantiate(final ApplicationContext ctx, final Class<? extends T> clazz) {
        return SpringUtils.getOrInstantiate(ctx, clazz);
    }
}
