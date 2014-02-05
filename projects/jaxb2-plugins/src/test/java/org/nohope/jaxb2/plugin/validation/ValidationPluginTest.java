package org.nohope.jaxb2.plugin.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nohope.jaxb2.plugin.Jaxb2PluginTestSupport;
import org.nohope.test.runner.TestLifecycleListeningClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.nohope.jaxb2.plugin.validation.ParseExceptionValidator.message;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 14:09
 */
@RunWith(TestLifecycleListeningClassRunner.class)
public class ValidationPluginTest extends Jaxb2PluginTestSupport {
    public ValidationPluginTest() {
        super("validation", "org/nohope/jaxb2/codegen/validation/correct");
    }

    @Test
    public void parameters() {
        assertNotNull(new ValidationPlugin().getUsage());
    }

    @Test
    public void bindingRedefinition() {
        assertParseFailure("redefinition_global", "Too many bindings found for xsd type complexObjectTypeX1");
        assertParseFailure("redefinition_local", "Too many bindings found for xsd type complexObjectTypeX1");
    }

    @Test
    public void bindElement() {
        assertParseFailure("illegal_bind_parameter", "bind element contains unknown 'unknown' attribute");
        assertParseFailure("missing_bind_parameter", "bind element should contain 'type' attribute");
        assertParseFailure("multiply_bind_child_element", "bind node must contains exactly one validator child node");
        assertParseFailure("unsupported_bind_child_element", "unsupported bind child node found: validation:bind");
        assertParseFailure("missing_complex_type",
                "exactly one complex type should be found for name 'xxx', but 0 found");
    }

    @Test
    public void validatorElement() {
        assertParseFailure("empty_validator_attribute",
                "validator element contains empty 'class' or 'context' attribute");
        assertParseFailure("illegal_validator_attribute",
                "validator element contains unknown 'unknown' attribute");
    }


    private static void assertParseFailure(final String schema, final String message) {
        assertBuildFailure("validation", "org/nohope/jaxb2/codegen/validation/" + schema, message(message));
    }
}
