package org.nohope.cassandra.factory;

import com.datastax.driver.core.*;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class PreparedStatementNamedParameters {
    private static final Logger LOG = LoggerFactory.getLogger(PreparedStatementNamedParameters.class);
    private static final double TRACING_PROBABILITY = 0.001;

    private static final Pattern MARKER_REGEXP = Pattern.compile("(^|[^\\\\])\\$([a-zA-Z0-9_]+)");
    private static final int MARKER_NAME_GROUP_ID = 2;
    private static final Pattern ESCAPE_SEQUENCE = Pattern.compile("\\\\\\$");
    private final List<String> positionsToNames;
    private final Session session;
    private final PreparedStatement preparedStatement;

    public PreparedStatementNamedParameters(final Session session, final String query) {
        this.session = session;
        this.positionsToNames = createPositionsToNames(query);
        final String plainOldQuery = convertToSimpleQuery(query);
        LOG.debug("Preparing C* query: {}", plainOldQuery);
        preparedStatement = session.prepare(plainOldQuery);
    }

    private static List<String> createPositionsToNames(final CharSequence query) {
        final List<String> result = new ArrayList<>();

        final Matcher m = MARKER_REGEXP.matcher(query);
        while (m.find()) {
            final String parameterName = m.group(MARKER_NAME_GROUP_ID);
            result.add(parameterName);
        }

        return result;
    }

    private static String convertToSimpleQuery(final String query) {
        final StringBuilder result = new StringBuilder();
        int previousNamedParameterEnd = 0;
        final Matcher m = MARKER_REGEXP.matcher(query);
        while (m.find()) {
            //-1 for the $
            final int namedParameterStart = m.start(MARKER_NAME_GROUP_ID) - 1;
            result.append(query.substring(previousNamedParameterEnd, namedParameterStart))
                  .append('?');
            previousNamedParameterEnd = m.end(MARKER_NAME_GROUP_ID);
        }
        result.append(query.substring(previousNamedParameterEnd));

        return ESCAPE_SEQUENCE.matcher(result).replaceAll("\\$");
    }

    public ResultSet execute() {
        return execute(Collections.<String, Object> emptyMap());
    }

    public ResultSet execute(final Object... params) {
        return session.execute(bind(params));
    }

    public <T> ResultSet execute(final Map<String, T> params) {
        return session.execute(bind(params));
    }

    public BoundStatement bind(final Object... params) {
        if ((params.length % 2) != 0) {
            throw new IllegalArgumentException("Should have even number of params");
        }

        final Map<String, Object> keyValues = new HashMap<>();
        for (int i = 0; (2 * i) < params.length; i++) {
            keyValues.put((String) params[2 * i], params[((2 * i) + 1)]);
        }
        return bind(keyValues);
    }

    public ResultSetFuture executeAsync() {
        return executeAsync(Collections.<String, Object> emptyMap());
    }

    public <T> ResultSetFuture executeAsync(final Map<String, T> params) {
        return session.executeAsync(bind(params));
    }

    public <T> BoundStatement bind(final Map<String, T> params) {
        final Collection<T> plainOldParams = convertToPlainOldParams(params);
        return preparedStatement.bind(plainOldParams.toArray());
    }

    private <T> Collection<T> convertToPlainOldParams(final Map<String, T> params) {
        if (params.keySet().containsAll(positionsToNames)) {
            return Collections2.transform(
                    positionsToNames, new Function<String, T>() {
                        @Override
                        public T apply(final String paramName) {
                            return params.get(paramName);
                        }
                    });
        }

        final Collection<String> unboundParameters = new ArrayList<>();
        for (final String namedParameter : positionsToNames) {
            if (!params.containsKey(namedParameter)) {
                unboundParameters.add(namedParameter);
            }
        }

        throw new IllegalArgumentException("Parameters are unbound: " + Joiner.on(". ").join(unboundParameters));
    }
}
