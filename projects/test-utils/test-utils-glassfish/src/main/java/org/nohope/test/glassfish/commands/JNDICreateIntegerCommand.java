package org.nohope.test.glassfish.commands;

/**
* Date: 2/11/14
* Time: 3:17 PM
*/
public class JNDICreateIntegerCommand extends JNDICreateCommand {
    public JNDICreateIntegerCommand(final Integer value, final String path) {
        super(Integer.class.getCanonicalName(), value.toString(), path);
    }
}
