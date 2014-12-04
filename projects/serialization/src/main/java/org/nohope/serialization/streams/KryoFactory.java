package org.nohope.serialization.streams;

import com.esotericsoftware.kryo.Kryo;
import org.objenesis.strategy.SerializingInstantiatorStrategy;

public interface KryoFactory {
    KryoFactory DEFAULT = new KryoFactory() {
        @Override
        public Kryo newKryo() {
            final Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new SerializingInstantiatorStrategy());
            return kryo;
        }
    };

    Kryo newKryo();
}
