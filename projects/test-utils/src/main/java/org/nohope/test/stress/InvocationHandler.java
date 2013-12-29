package org.nohope.test.stress;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
* @since 2013-12-29 19:10
*/
interface InvocationHandler<T> {
    T invoke() throws Exception;
}
