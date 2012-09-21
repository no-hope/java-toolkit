package org.nohope.reflection;

import org.nohope.typetools.StringUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/13/12 1:11 AM
 */
public enum ModifierMatcher implements IModifierMatcher {
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
    };

    public static IModifierMatcher and(final IModifierMatcher... matchers) {
        return new IModifierMatcher() {
            @Override
            public boolean matches(@Nonnull final Integer flags) {
                for (final IModifierMatcher m : matchers) {
                    if (!m.matches(flags)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public String toString() {
                return "(" + StringUtils.join(matchers, " && ") + ")";
            }
        };
    }

    public static IModifierMatcher or(final IModifierMatcher... matchers) {
        return new IModifierMatcher() {
            @Override
            public boolean matches(@Nonnull final Integer flags) {
                for (final IModifierMatcher m : matchers) {
                    if (m.matches(flags)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public String toString() {
                return "(" + StringUtils.join(matchers, " || ") + ")";
            }
        };
    }

    public static IModifierMatcher not(final IModifierMatcher matcher) {
        return new IModifierMatcher() {
            @Override
            public boolean matches(@Nonnull final Integer flags) {
                return !matcher.matches(flags);
            }

            @Override
            public String toString() {
                return "!" + matcher;
            }
        };
    }
}
