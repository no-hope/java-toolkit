package org.nohope.reflection;

import org.nohope.IMatcher;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;

import static org.nohope.Matchers.not;
import static org.nohope.Matchers.or;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/13/12 1:11 AM
 */
public enum ModifierMatcher implements IMatcher<Integer> {
    PUBLIC  {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return (flags & Modifier.PUBLIC) != 0;
        }
    },
    PRIVATE {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return (flags & Modifier.PRIVATE) != 0;
        }
    },
    PROTECTED {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return (flags & Modifier.PROTECTED) != 0;
        }
    },
    FINAL {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return (flags & Modifier.FINAL) != 0;
        }
    },
    STATIC {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return (flags & Modifier.STATIC) != 0;
        }
    },
    ABSTRACT {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return (flags & Modifier.ABSTRACT) != 0;
        }
    },
    PACKAGE_DEFAULT {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return not(or(PUBLIC, PRIVATE, PROTECTED)).matches(flags);
        }
    },
    ALL {
        @Override
        public boolean matches(@Nonnull final Integer flags) {
            return true;
        }
    }
}
