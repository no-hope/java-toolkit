package org.nohope.jaxb2.plugin.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nohope.jaxb2.plugin.Jaxb2PluginTestSupport;
import org.nohope.test.runner.InstanceTestClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 14:09
 */
@RunWith(InstanceTestClassRunner.class)
public class ValidatorPluginTest extends Jaxb2PluginTestSupport {
    public ValidatorPluginTest() {
        super("validation", "org/nohope/jaxb2/codegen/validation/correct");
    }

    @Test
    public void parameters() {
        assertNotNull(new ValidationPlugin().getUsage());
    }
}
