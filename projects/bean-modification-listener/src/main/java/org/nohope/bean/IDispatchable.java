package org.nohope.bean;

import javax.annotation.Nonnull;

/**
 * Marker interface for beans which state changes should be handled by
 * external dispatcher.
 * <p />
 * <p />
 * <b>EXAMPLE:</b>
 * <pre>
 * public class MyBean extends {@link AbstractDispatchable AbstractDispatchable} {
 *     public MyBean(@Nonnull final IDispatcher dispatcher) {
 *         super(dispatcher);
 *     }
 *
 *     // on each successful method invocation dispatcher will
 *     // be notified about new "test_property" property value
 *     {@link Dispatch &#064;Dispatch}(name = "test_property")
 *     public void setSomething(final Object obj) {
 *         // do something cool with object
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 12/7/12 7:30 PM
 */
public interface IDispatchable {
    @Nonnull IDispatcher getDispatcher();
}
