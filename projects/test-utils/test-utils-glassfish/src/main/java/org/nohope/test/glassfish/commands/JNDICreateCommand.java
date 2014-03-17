package org.nohope.test.glassfish.commands;

import org.nohope.typetools.TStr;

/**
* Date: 2/11/14
* Time: 3:16 PM
*/
public class JNDICreateCommand extends Command {
    public JNDICreateCommand(final String typename, final String value, final String path) {
        super("create-custom-resource",
                new String[]{TStr.format("--restype={}", typename),
                "--factoryclass=org.glassfish.resources.custom.factory.PrimitivesAndStringFactory",
                "--property",
                TStr.format("value=\"{}\"", value),
                path});
    }
}
