package org.nohope.spring;

import org.junit.Test;
import org.nohope.reflection.TypeReference;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-14 17:00
 */
public class BeanDefinitionTest {

    @Test
    public void equality() {
        final BeanDefinition<Map<String, Object>> def =
                BeanDefinition.of("test", new TypeReference<Map<String, Object>>() {});
        assertEquals("test", def.getName());
        assertEquals(Map.class, def.getBeanClass());
        assertEquals(new TypeReference<Map<String, Object>>() {}, def.getTypeReference());

        final BeanDefinition<Map> defErasured = BeanDefinition.of("test", Map.class);
        assertEquals(def, defErasured);

        assertEquals(def.hashCode(), defErasured.hashCode());
        assertEquals(def, def);
        assertNotEquals(def, "");
    }
}
