package org.nohope.typetools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Date: 11/8/12
 * Time: 3:28 PM
 */
public class JSONTest {

    public static class Bean {
        private final String a;
        private final Integer b;

        // used for de-serialization only
        private Bean() {
            a = null;
            b = null;
        }

        public Bean(final String a, final Integer b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Bean bean = (Bean) o;
            return a.equals(bean.a) && b.equals(bean.b);
        }

        @Override
        public int hashCode() {
            int result = a.hashCode();
            result = 31 * result + b.hashCode();
            return result;
        }
    }

    public static class CycleBean {
        private final CycleBean a;

        public CycleBean() {
            this.a = this;
        }
    }


    @Test
    public void jsonSerialization() throws Exception {
        final Bean object = new Bean("x", 1);

        for (final JSON json : Arrays.asList(JSON.JSON, JSON.customize(new ObjectMapper()))) {
            assertEquals("{\"@class\":\"org.nohope.typetools.JSONTest$Bean\",\"a\":\"x\",\"b\":1}",
                    json.jsonify(object).toString());
            assertEquals("{\n"
                         + "  \"@class\" : \"org.nohope.typetools.JSONTest$Bean\",\n"
                         + "  \"a\" : \"x\",\n"
                         + "  \"b\" : 1\n"
                         + "}",
                    json.pretty(object).toString());
        }

        final JSON custom = JSON.customize(new ObjectMapper(), false);
        assertEquals("<? org.nohope.typetools.JSONTest.Bean />", custom.jsonify(object).toString());
        assertEquals("<? org.nohope.typetools.JSONTest.Bean />", custom.pretty(object).toString());

        assertEquals(object, JSON.JSON.copyAs(object, Bean.class));

        assertEquals("null", JSON.JSON.pretty(null).toString());
        assertEquals("null", JSON.JSON.jsonify(null).toString());
    }

    @Test
    public void errorneousJsonSerialization() throws Exception {
        final CycleBean object = new CycleBean();
        assertEquals("<? org.nohope.typetools.JSONTest.CycleBean />", JSON.JSON.jsonify(object).toString());
        assertEquals("<? org.nohope.typetools.JSONTest.CycleBean />", JSON.JSON.pretty(object).toString());

        assertEquals(null, JSON.JSON.copyAs(null, CycleBean.class));
    }
}
