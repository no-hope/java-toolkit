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
public class ValidationPluginMissingComplexTypeTest extends Jaxb2PluginTestSupport {
    public ValidationPluginMissingComplexTypeTest() {
        super("validation", "org/nohope/jaxb2/codegen/validation/missing_complex_type");
    }

    @Override
    protected boolean validateBuildFailure(final Throwable e) throws Exception {
        return e instanceof IllegalStateException
                && e.getCause() instanceof SAXParseException
                && "exactly one complex type should be found for name 'xxx', but 0 found".equals(e.getCause().getMessage())
                ;
    }
}
