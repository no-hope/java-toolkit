package org.nohope.test.stress.actions;

import org.nohope.test.stress.MeasureData;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
* @since 2013-12-27 16:20
*/
public abstract class NamedAction extends AbstractAction<MeasureData> {
    private final String name;

    public NamedAction(final String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}
