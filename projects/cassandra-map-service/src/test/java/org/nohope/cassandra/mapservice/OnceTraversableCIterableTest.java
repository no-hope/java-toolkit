package org.nohope.cassandra.mapservice;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

public class OnceTraversableCIterableTest {

    @Test(expected = IllegalStateException.class)
    public void tryToIterateMoreThanOneTimeTest() {
        final List<String> testList = Lists.newArrayList("one", "two", "three");
        final Iterable<String> iterator = new OnceTraversableCIterable<>(testList);
        iterator.iterator();
        iterator.iterator();
    }
}
