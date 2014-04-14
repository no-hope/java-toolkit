package org.nohope.reflection.test;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/17/12 4:30 PM
 */
class PackagePrivateChildWithOverrides extends PackagePrivateParent {
    @Override
    protected void protectedMethod() {
    }

    @Override
    public void publicMethod() {
    }

    @Override
    void packageDefaultMethod() {
    }
}
