package org.nohope.spring.app;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/22/12 6:47 PM
 */
public class AbstractDependencyProvider<T>
        extends AbstractList<T>
        implements IDependencyProvider<T> {

    private final List<T> modules = new ArrayList<>();

    void setModules(final Collection<T> modules) {
        this.modules.clear();
        this.modules.addAll(modules);
    }

    @Override
    public T get(final int index) {
        return modules.get(index);
    }

    @Override
    public int size() {
        return modules.size();
    }

    /*
    @Override
    public List<T> getModules() {
        return new ArrayList<>(modules);
    }
    */
}
