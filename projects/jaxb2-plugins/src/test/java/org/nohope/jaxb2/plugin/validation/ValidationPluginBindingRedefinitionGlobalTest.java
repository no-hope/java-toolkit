package org.nohope.jaxb2.plugin.validation;

import org.junit.runner.RunWith;
import org.nohope.jaxb2.plugin.Jaxb2PluginTestSupport;
import org.nohope.test.runner.InstanceTestClassRunner;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 14:09
 */
@RunWith(InstanceTestClassRunner.class)
public class ValidationPluginBindingRedefinitionGlobalTest extends Jaxb2PluginTestSupport {
    public ValidationPluginBindingRedefinitionGlobalTest() {
        super("validation", "org/nohope/jaxb2/codegen/validation/redefinition_global");
    }

    @Override
    protected boolean validateBuildFailure(final Throwable e) throws Exception {
        return e instanceof IllegalStateException
               && e.getCause() instanceof SAXParseException
               && "Too many bindings found for xsd type complexObjectTypeX1".equals(e.getCause().getMessage())
                ;
    }
}
