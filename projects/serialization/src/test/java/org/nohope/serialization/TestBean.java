package org.nohope.serialization;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
*/
class TestBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int p1;
    private final long p2;

    TestBean(@JsonProperty("p1") int p1,
             @JsonProperty("p2") long p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public int getP1() {
        return p1;
    }

    public long getP2() {
        return p2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestBean testBean = (TestBean) o;
        return p1 == testBean.p1 && p2 == testBean.p2;
    }

    @Override
    public int hashCode() {
        int result = p1;
        result = 31 * result + (int) (p2 ^ (p2 >>> 32));
        return result;
    }
}
