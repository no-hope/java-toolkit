package org.nohope.reflection;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.lang.reflect.Modifier;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/13/12 1:11 AM
 */
public enum ModifierMatcher implements Predicate<Integer> {
    PUBLIC  {
        @Override
        public boolean apply(final Integer flags) {
            return (flags & Modifier.PUBLIC) != 0;
        }
    },
    PRIVATE {
        @Override
        public boolean apply(final Integer flags) {
            return (flags & Modifier.PRIVATE) != 0;
        }
    },
    PROTECTED {
        @Override
        public boolean apply(final Integer flags) {
            return (flags & Modifier.PROTECTED) != 0;
        }
    },
    FINAL {
        @Override
        public boolean apply(final Integer flags) {
            return (flags & Modifier.FINAL) != 0;
        }
    },
    STATIC {
        @Override
        public boolean apply(final Integer flags) {
            return (flags & Modifier.STATIC) != 0;
        }
    },
    ABSTRACT {
        @Override
        public boolean apply(final Integer flags) {
            return (flags & Modifier.ABSTRACT) != 0;
        }
    },
    PACKAGE_DEFAULT {
        @Override
        public boolean apply(final Integer flags) {
            return Predicates.not(Predicates.or(PUBLIC, PRIVATE, PROTECTED)).apply(flags);
        }
    },
    ALL {
        @Override
        public boolean apply(final Integer flags) {
            return true;
        }
    }
}
