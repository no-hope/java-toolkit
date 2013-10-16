package org.nohope.jaxb2.plugin;

import org.junit.Test;
import org.jvnet.jaxb2.maven2.AbstractXJC2Mojo;
import org.nohope.ITranslator;
import org.nohope.test.ResourceUtils;
import org.nohope.test.runner.InstanceTestSetupListener;
import org.nohope.typetools.collection.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 14:50
 */
public abstract class Jaxb2PluginTestSupport implements InstanceTestSetupListener {
    //static {
    //    System.setProperty("javax.xml.accessExternalStylesheet", "all");
    //    System.setProperty("javax.xml.accessExternalSchema", "all");
    //    System.setProperty("javax.xml.accessExternalDTD", "all");
    //}

    private final String arg;
    private final String resources;
    private final String classpath;

    public Jaxb2PluginTestSupport(final String arg,
                                  final String resources,
                                  final String classpath) {
        this.arg = arg;
        this.resources = resources;
        this.classpath = classpath;
    }

    public Jaxb2PluginTestSupport(final String arg,
                                  final String resources) {
        this.arg = arg;
        this.resources = "src/test/resources/" + resources;
        this.classpath = null;
    }

    private String getClasspath() {
        return classpath != null
                ? classpath
                : resources.replace("src/test/resources/", "").replace("/", ".");
    }

    private String getClasspathPath() {
        return classpath != null
                ? classpath
                : resources.replace("src/test/resources/", "");
    }

    @Override
    public void beforeClassSetup() throws Exception {
        try {
            new RunMetadataPlugin().testExecute();
        } catch (final Throwable e) {
            if (!validateBuildFailure(e)) {
                throw e;
            } else {
                return;
            }
        }

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        final String root = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile();

        final PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();

        final List<JavaSourceFromString> units = new ArrayList<>();
        final List<String> classFiles = new ArrayList<>();
        final List<String> classes = new ArrayList<>();
        for (final Resource resource : resolver.getResources(
                "classpath*:" + getClasspathPath() + "/**/*.java")) {
            final String file = resource.getFile().getAbsolutePath().replace(root, "");
            if (file.endsWith(".java")) {
                final String prefix = file.substring(0, file.length() - 5);
                final String code = ResourceUtils.getResourceAsString("/" + file);
                assertNotNull(code);
                final String fqdn = prefix.replace("/", ".");
                units.add(new JavaSourceFromString(fqdn, code));
                classFiles.add("/" + prefix + ".class");
                if (!prefix.endsWith("package-info")) {
                    classes.add(fqdn);
                }
            }
        }

        final JavaCompiler.CompilationTask task =
                compiler.getTask(null, null, diagnostics, Arrays.asList("-d", root), null, units);

        if (!task.call()) {
            final StringBuilder builder = new StringBuilder("Unable to compile generated source files: ");
            int i = 0;
            for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                builder.append("\n ").append(++i).append(") ").append(diagnostic.getMessage(null)).append('\n');
            }

            throw new IllegalStateException(builder.toString());
        }

        final URLClassLoader ucl = new URLClassLoader(
                CollectionUtils.mapArray(classFiles.toArray(new String[classFiles.size()]), URL.class,
                        new ITranslator<String, URL>() {
                            @Override
                            public URL translate(final String source) {
                                final URL resource = ResourceUtils.getResource(source);
                                assertNotNull(resource);
                                return resource;
                            }
                        }));

        for (final String fqdn : classes) {
            ucl.loadClass(fqdn);
        }
    }

    @Test
    public void setupCorrectness() {
    }

    @Override
    public void afterClassSetup() {
    }

    /**
     * A file object used to represent source coming from a string.
     */
    public class JavaSourceFromString extends SimpleJavaFileObject {
        /**
         * The source code of this "file".
         */
        private final String code;

        /**
         * Constructs a new JavaSourceFromString.
         * @param name the name of the compilation unit represented by this file object
         * @param code the source code for the compilation unit represented by this file object
         */
        public JavaSourceFromString(final String name,
                                    final String code) {
            super(URI.create(
                    "string:///"
                    + name.replace('.', '/')
                    + JavaFileObject.Kind.SOURCE.extension),
                    JavaFileObject.Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private class RunMetadataPlugin extends org.jvnet.jaxb2.maven2.test.RunXJC2Mojo {

        @Override
        public File getSchemaDirectory() {
            return new File(getBaseDir(), resources);
        }

        @Override
        protected File getGeneratedDirectory() {
            return new File(getBaseDir(), "target/test-classes");
        }

        @Override
        protected void configureMojo(final AbstractXJC2Mojo mojo) {
            super.configureMojo(mojo);
            mojo.setForceRegenerate(true);
            mojo.setAddCompileSourceRoot(true);
            mojo.setExtension(true);
            if (classpath == null) {
                mojo.setGeneratePackage(getClasspath());
            }
        }

        @Override
        public List<String> getArgs() {
            final List<String> args = new ArrayList<>(super.getArgs());
            args.add("-X" + arg);
            return args;
        }
    }

    protected boolean validateBuildFailure(final Throwable e) throws Exception {
        return false;
    }
}