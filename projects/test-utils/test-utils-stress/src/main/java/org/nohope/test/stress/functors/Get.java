package org.nohope.test.stress.functors;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
* @since 2013-12-29 18:39
*/
@FunctionalInterface
public interface Get<T> {
    T get() throws Exception;
}
