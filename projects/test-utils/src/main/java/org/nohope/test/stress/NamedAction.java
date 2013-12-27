package org.nohope.test.stress;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:20
*/
public abstract class NamedAction {
    private final String name;

    public NamedAction(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected abstract void doAction(final int threadId,
                                     final int operationNumber) throws Exception;
}
