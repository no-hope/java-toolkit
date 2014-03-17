package org.nohope.jongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jongo.marshall.jackson.JacksonMapper;
import org.jongo.marshall.jackson.configuration.MapperModifier;
import org.nohope.typetools.TypeSafeObjectMapper;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-14 18:59
 */
public class TypeSafeJacksonMapperBuilder extends JacksonMapper.Builder {
    public TypeSafeJacksonMapperBuilder() {
        super();
        addModifier(new MapperModifier() {
            @Override
            public void modify(final ObjectMapper mapper) {
                TypeSafeObjectMapper.configureMapper(mapper, true);
            }
        });
    }
}
