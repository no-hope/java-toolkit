package org.nohope.test.glassfish.commands;

/**
* Date: 2/11/14
* Time: 3:16 PM
*/
public class JNDICreateStringCommand extends JNDICreateCommand {
    public JNDICreateStringCommand(final String value, final String path) {
        super(String.class.getCanonicalName(), value, path);
    }
}
