package org.nohope.jongo;

import org.jongo.Mapper;
import org.junit.Test;

/**
 * Date: 04.08.12
 * Time: 16:59
 */
public class TypeSafeJacksonMapperBuilderTest {

    @Test
    public void basicMarshalling() {
        final TypeSafeJacksonMapperBuilder builder = new TypeSafeJacksonMapperBuilder();
        final Mapper build = builder.build();
        build.getUnmarshaller().unmarshall(build.getMarshaller().marshall(new Bean()), Bean.class);
    }

    static class CyclicBean {
        CyclicBean bean;
        CyclicBean() {
            bean = this;
        }
    }

    static class Bean {
    }
}
