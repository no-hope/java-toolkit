package org.jvnet.jax_ws_commons.json;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.BindingIDFactory;

import javax.xml.ws.WebServiceException;

/**
 * @author Jitendra Kotamraju
 */
public class JSONBindingIDFactory extends BindingIDFactory {

    @Nullable
    @Override
    public BindingID parse(@NotNull final String lexical) throws WebServiceException {
        return new JSONBindingID();
    }
}
