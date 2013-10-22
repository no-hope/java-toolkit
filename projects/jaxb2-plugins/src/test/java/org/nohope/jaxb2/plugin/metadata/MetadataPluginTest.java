package org.nohope.jaxb2.plugin.metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nohope.jaxb2.plugin.Jaxb2PluginTestSupport;
import org.nohope.test.runner.InstanceTestClassRunner;

import java.util.List;

import static java.lang.Class.forName;
import static org.junit.Assert.*;
import static org.nohope.reflection.IntrospectionUtils.instanceOf;
import static org.nohope.reflection.IntrospectionUtils.invoke;
import static org.nohope.reflection.ModifierMatcher.*;
import static org.nohope.Matchers.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 16:31
 */
@RunWith(InstanceTestClassRunner.class)
public class MetadataPluginTest extends Jaxb2PluginTestSupport {
    public MetadataPluginTest() {
        super("metadata",
              "src/test/resources/org/nohope/jaxb2/codegen/metadata",
               "org/nohope/jaxb_codegen/metadata", new String[0]);
    }

    @Test
    public void checkGeneratedObjectsValidity() throws Exception {
        final Class<?> cobj1 = forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType");
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
                forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType$IDescriptor")));

        assertEquals("float_field", chain.get(1).getProperty());
        assertTrue(instanceOf(chain.get(1).getParentDescriptor(),
                forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType2$IDescriptor")));
        assertEquals(forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType2"),
                chain.get(1).getParentDescriptor().getFieldType().getTypeClass());
    }

    @Test
    public void properties() {
        final MetadataPlugin plugin = new MetadataPlugin();
        assertNotNull(plugin.getUsage());
    }
}
