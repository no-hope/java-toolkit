package org.nohope.test.glassfish.commands;

/**
* Date: 2/11/14
* Time: 3:17 PM
*/
public class Command {
    private final String command;
    private final String[] args;

    public Command(final String command, final String[] args) {
        this.command = command;
        this.args = args;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }
}
