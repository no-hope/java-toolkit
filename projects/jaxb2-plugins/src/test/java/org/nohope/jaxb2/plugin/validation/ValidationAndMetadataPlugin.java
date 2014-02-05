package org.nohope.jaxb2.plugin.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nohope.jaxb2.plugin.Jaxb2PluginTestSupport;
import org.nohope.test.runner.TestLifecycleListeningClassRunner;

import static java.lang.Class.forName;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-11-05 11:58
 */
@RunWith(TestLifecycleListeningClassRunner.class)
public class ValidationAndMetadataPlugin extends Jaxb2PluginTestSupport {
    public ValidationAndMetadataPlugin() {
        super(new String[] {"metadata", "validation"},
                "org/nohope/jaxb2/codegen/validation/correct_metadata");
    }

    @Test
    public void test() throws ClassNotFoundException {
        forName("org.nohope.jaxb2.codegen.validation.correct_metadata.ComplexObjectTypeX5");
    }
}
