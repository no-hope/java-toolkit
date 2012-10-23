package org.nohope.jongo;

import org.jongo.Jongo;
import org.junit.Test;
import org.nohope.jongo.JacksonProcessor;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Date: 04.08.12
 * Time: 16:59
 */
public class JacksonProcessorTest {
    @Test
    public void testMapKeyEscaping() {
        final Map<String, Object> map1 = generateMap();

        final JacksonProcessor proc = new JacksonProcessor();
        final String marshalled = proc.marshall(map1);
        final Object restored = proc.unmarshall(marshalled, Object.class);

        assertEquals(map1, restored);
    }

    static Map<String, Object> generateMap() {
        final Map<String, Object> map1 = new HashMap<>();

        map1.put("hui.pizda.dzhigurda", "value.#pizda");
        map1.put("hui#pizda#dzhigurda1", "value.#pizda1");

        final Map<String, Object> map2 = new HashMap<>();
        map2.put("level2.hui.pizda.dzhigurda", "level2.value.#pizda");
        map2.put("level2.hui#pizda#dzhigurda1", "level2.value.#pizda1");

        map1.put("level2.map#one", map2);

        final Map<String, Object> map3 = new HashMap<>();
        map3.put("level3.hui.pizda.dzhigurda", "level3.value.#pizda");
        map3.put("level3.hui#pizda#dzhigurda1", "level3.value.#pizda1");
        map3.put("TEST_METADATA_KEY1", "TEST_VALUE1");
        map1.put("level3.map#two", map3);
        map1.put("TEST_METADATA_KEY", "TEST_VALUE");
        return map1;
    }

    @Test
    public void testEscaping() {
        final String source = "text hash1=# hash2=\\# hash3=\\\\# dot=. at=@ underscore=_ underscore2=\\_";
        final String escaped = JacksonProcessor.escape(source);
        assertEquals("text hash1=\\# hash2=\\\\# hash3=\\\\\\# dot=_ at=\\@ underscore=\\_ underscore2=\\\\_", escaped);

        final String restored = JacksonProcessor.unescape(escaped);
        assertEquals(source, restored);
    }
}
