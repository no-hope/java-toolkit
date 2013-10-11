package org.nohope.jaxb2.plugin.metadata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.jaxb2.maven2.AbstractXJC2Mojo;

import java.io.File;
import java.util.ArrayList;
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

    private static class RunMetadataPlugin extends org.jvnet.jaxb2.maven2.test.RunXJC2Mojo {

        @Override
        public File getSchemaDirectory() {
            return new File(getBaseDir(), "src/test/resources/metadata");
        }

        @Override
        protected void configureMojo(final AbstractXJC2Mojo mojo) {
            super.configureMojo(mojo);
            mojo.setForceRegenerate(true);
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
        final Class<?> cobj1 = forName("org.nohope.metadata.ComplexObjectType");
        final Object descriptor = invoke(cobj1, ALL, "descriptor");

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
