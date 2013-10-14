package org.nohope.jaxb2.plugin.metadata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.jaxb2.maven2.AbstractXJC2Mojo;
import org.nohope.ITranslator;
import org.nohope.test.ResourceUtils;
import org.nohope.typetools.collection.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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

import static java.lang.Class.forName;
import static org.junit.Assert.*;
import static org.nohope.reflection.IntrospectionUtils.instanceOf;
import static org.nohope.reflection.IntrospectionUtils.invoke;
import static org.nohope.reflection.ModifierMatcher.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 16:31
 */
public class RunMetadataPluginTest {
    //static {
    //    System.setProperty("javax.xml.accessExternalStylesheet", "all");
    //    System.setProperty("javax.xml.accessExternalSchema", "all");
    //    System.setProperty("javax.xml.accessExternalDTD", "all");
    //}

    /**
     * A file object used to represent source coming from a string.
     */
    public class JavaSourceFromString extends SimpleJavaFileObject {
        /**
         * The source code of this "file".
         */
        final String code;

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

    private static class RunMetadataPlugin extends org.jvnet.jaxb2.maven2.test.RunXJC2Mojo {

        @Override
        public File getSchemaDirectory() {
            return new File(getBaseDir(), "src/test/resources/metadata");
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
        }

        @Override
        public List<String> getArgs() {
            final List<String> args = new ArrayList<>(super.getArgs());
            args.add("-Xmetadata");
            return args;
        }
    }

    @BeforeClass
    public static void generateCode() throws Exception {
        new RunMetadataPlugin().testExecute();
    }

    @Test
    public void checkGeneratedObjectsValidity() throws Exception {
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
        for (final Resource resource : resolver.getResources("classpath*:org/nohope/metadata/**/*.java")) {
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
            throw new IllegalStateException();
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

        final Class<?> cobj1 = forName("org.nohope.metadata.ComplexObjectType");
        final Object descriptor = invoke(cobj1, ALL, "descriptor");

        final CallChain boolFieldChain =
                (CallChain) invoke(invoke(descriptor,
                        not(ABSTRACT), "isSimple"),
                        not(ABSTRACT), "getCallChain");

        assertFalse(boolFieldChain.getChain().isEmpty());
        assertEquals("simple", boolFieldChain.getChain().get(0).getProperty());

        final CallChain callChain =
                (CallChain) invoke(invoke(invoke(descriptor,
                        not(ABSTRACT), "getObjectField"),
                        not(ABSTRACT), "getFloatField"),
                        not(ABSTRACT), "getCallChain");

        final List<NamedDescriptor<?>> chain = callChain.getChain();
        assertEquals(2, chain.size());
        assertEquals("object_field", chain.get(0).getProperty());
        assertTrue(instanceOf(chain.get(0).getParentDescriptor(),
                forName("org.nohope.metadata.ComplexObjectType$IDescriptor")));

        assertEquals("float_field", chain.get(1).getProperty());
        assertTrue(instanceOf(chain.get(1).getParentDescriptor(),
                forName("org.nohope.metadata.ComplexObjectType2$IDescriptor")));
        assertEquals(forName("org.nohope.metadata.ComplexObjectType2"),
                chain.get(1).getParentDescriptor().getFieldType().getTypeClass());
    }

    @Test
    public void properties() {
        final MetadataPlugin plugin = new MetadataPlugin();
        assertNotNull(plugin.getUsage());
    }
}
