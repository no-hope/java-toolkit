package org.nohope.jongo;

import org.jongo.marshall.jackson.JacksonMapper.Builder;
import org.nohope.typetools.TypeSafeObjectMapper;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-14 18:59
 */
public class TypeSafeJacksonMapperBuilder extends Builder {
    public TypeSafeJacksonMapperBuilder() {
        addModifier(mapper -> TypeSafeObjectMapper.configureMapper(mapper, true));
    }
}
