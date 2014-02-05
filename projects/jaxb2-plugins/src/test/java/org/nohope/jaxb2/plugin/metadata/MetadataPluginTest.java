package org.nohope.jaxb2.plugin.metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nohope.jaxb2.plugin.Jaxb2PluginTestSupport;
import org.nohope.test.runner.TestLifecycleListeningClassRunner;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Class.forName;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;
import static org.nohope.Matchers.not;
import static org.nohope.reflection.IntrospectionUtils.instanceOf;
import static org.nohope.reflection.IntrospectionUtils.invoke;
import static org.nohope.reflection.ModifierMatcher.ABSTRACT;
import static org.nohope.reflection.ModifierMatcher.ALL;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 16:31
 */
@RunWith(TestLifecycleListeningClassRunner.class)
public class MetadataPluginTest extends Jaxb2PluginTestSupport {
    public MetadataPluginTest() {
        super("metadata",
              "src/test/resources/org/nohope/jaxb2/codegen/metadata",
               "org/nohope/jaxb_codegen/metadata", new String[0]);
    }

    @Test
    public void classDescriptorValidity() throws Exception {
        final Class<?> clazz = forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType");
        final Object descriptor = invoke(clazz, ALL, "getClassDescriptor");

        {
            final IDescriptor<?> boolFieldDescriptor =
                    (IDescriptor<?>) invoke(descriptor,
                            not(ABSTRACT), "isSimple");

            final List<IDescriptor<?>> callChain = new ArrayList<>();
            for (final IDescriptor o : boolFieldDescriptor) {
                callChain.add(o);
            }

            assertEquals(2, callChain.size());
            assertNull(callChain.get(0).getName());
            assertNull(callChain.get(0).getParent());
            assertEquals(clazz, callChain.get(0).getType().getTypeClass());

            assertEquals("simple", callChain.get(1).getName());
            assertEquals(Boolean.class, callChain.get(1).getType().getTypeClass());
        }

        {
            final IDescriptor<?> floatFieldDescriptor =
                    (IDescriptor<?>) invoke(invoke(descriptor,
                            not(ABSTRACT), "getObjectField"),
                            not(ABSTRACT), "getFloatField");

            final List<IDescriptor<?>> chain = new ArrayList<>();
            for (final IDescriptor o : floatFieldDescriptor) {
                chain.add(o);
            }

            assertEquals(3, chain.size());
            assertEquals("float_field", chain.get(2).getName());

            assertEquals(forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType2"),
                    chain.get(2).getParent().getType().getTypeClass());
            assertTrue(instanceOf(chain.get(2).getParent(),
                    forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType2$IClassDescriptor")));

            assertEquals("object_field", chain.get(1).getName());
            assertTrue(instanceOf(chain.get(1).getParent(),
                    forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType$IClassDescriptor")));

            assertEquals("org.nohope.jaxb_codegen.metadata.ComplexObjectType"
                         + "#object_field[org.nohope.jaxb_codegen.metadata.ComplexObjectType2]"
                         + "#float_field[java.lang.Float]", floatFieldDescriptor.toString());
        }
    }

    @Test
    public void instanceValidity() throws Exception {
        final Object obj = forName("org.nohope.jaxb_codegen.metadata.ComplexObjectType")
                .getConstructor().newInstance();

        final Object descriptor = invoke(obj, ALL, "getInstanceDescriptor");

        {
            final IValueDescriptor<?> boolFieldDescriptor =
                    (IValueDescriptor<?>) invoke(descriptor,
                            not(ABSTRACT), "isSimple");

            final List<IValueDescriptor<?>> chain = new ArrayList<>();
            for (final IDescriptor o : boolFieldDescriptor) {
                chain.add((IValueDescriptor) o);
            }

            assertEquals(2, chain.size());
            assertEquals(obj, chain.get(0).getValue());
            assertEquals(false, chain.get(1).getValue());
        }

        {
            final IDescriptor<?> floatFieldDescriptor =
                    (IDescriptor<?>) invoke(invoke(descriptor,
                            not(ABSTRACT), "getObjectField"),
                            not(ABSTRACT), "getFloatField");

            final List<IValueDescriptor<?>> chain = new ArrayList<>();
            for (final IDescriptor o : floatFieldDescriptor) {
                chain.add((IValueDescriptor) o);
            }

            assertEquals(3, chain.size());
            try {
                chain.get(2).getValue();
                fail();
            } catch (CallException e) {
                assertEquals("Error while getting value of descriptor "
                             + "org.nohope.jaxb_codegen.metadata.ComplexObjectType"
                             + "#object_field[org.nohope.jaxb_codegen.metadata.ComplexObjectType2]"
                             + "#float_field[java.lang.Float]", e.getMessage());
                assertNotNull(e.getCause());
                assertEquals(chain.get(2), e.getContext());
                assertEquals(NullPointerException.class, e.getCause().getClass());
            }
        }
    }

    @Test
    public void properties() {
        final MetadataPlugin plugin = new MetadataPlugin();
        assertNotNull(plugin.getUsage());
    }
}
