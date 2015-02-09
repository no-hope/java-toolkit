package org.nohope.cassandra.factory;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.ClusterCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class CassandraFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraFactory.class);
    private static final Pattern ENDPOINT_PATTERN = Pattern.compile(",\\s*");
    private static final Pattern IPV6_PATTERN = Pattern.compile("^\\[([:a-fA-F0-9]+)\\](:(\\d+))?$");
    private static final Pattern IPV4_PATTERN = Pattern.compile("^([\\.0-9]+)(:(\\d+))?$");
    private static final Pattern QUOTED_PATTERN = Pattern.compile("^\"\\w+\"$");
    private static final Pattern ENDPOINT_TOKENS_PATTERN = Pattern.compile(":");
    private static final int DEFAULT_NUMBER_NODES_TO_USE = 3;
    private int numNodesToUseFromAnotherDC = DEFAULT_NUMBER_NODES_TO_USE;
    private ClusterCustomization customization = ClusterCustomization.NONE;
    private String endpoints;
    private Cluster cluster;
    private Session session;
    private String keyspace;
    private String replication;
    private String localDC;

    public void init() {
        cluster = connectToCluster(endpoints);
        session = new CassandraSessionCircuitBreaker(cluster.connect());
        createKeyspaceIfNeeded();
        session.execute(String.format("USE %s", QueryBuilder.quote(keyspace)));
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setCustomization(final ClusterCustomization customizer) {
        this.customization = customizer;
    }

    public void setKeyspace(final String keyspace) {
        if (QUOTED_PATTERN.matcher(keyspace).matches()) {
            throw new IllegalArgumentException("No quoted keyspace allowed");
        }
        this.keyspace = keyspace;
    }

    public Session getSession() {
        return session;
    }

    public void setLocalDC(final String localDC) {
        this.localDC = localDC;
    }

    public void setEndpoints(final String endpoints) {
        this.endpoints = endpoints;
    }

    public void setReplication(final String replication) {
        this.replication = replication;
    }

    public void setNumNodesToUseFromAnotherDC(final int numNodesToUseFromAnotherDC) {
        this.numNodesToUseFromAnotherDC = numNodesToUseFromAnotherDC;
    }

    private static InetSocketAddress getAddress(final MatchResult addressMatcher) {
        final String rawPort = addressMatcher.group(3);
        final int port;
        if (rawPort == null) {
            port = ProtocolOptions.DEFAULT_PORT;
        } else {
            port = Integer.parseInt(rawPort);
        }
        return InetSocketAddress.createUnresolved(addressMatcher.group(1), port);
    }

    static InetSocketAddress getAddress(final String endpoint) {
        final String address = endpoint.trim();
        final Matcher ipv6matcher = IPV6_PATTERN.matcher(address);
        final Matcher ipv4matcher = IPV4_PATTERN.matcher(address);

        if (ipv6matcher.matches()) {
            return getAddress(ipv6matcher);
        } else if (ipv4matcher.matches()) {
            return getAddress(ipv4matcher);
        } else {
            final String[] tokens = ENDPOINT_TOKENS_PATTERN.split(endpoint);
            if (tokens.length == 1) {
                LOG.debug("Endpoint {}", tokens[0]);
                return new InetSocketAddress(tokens[0], ProtocolOptions.DEFAULT_PORT);
            } else if (tokens.length == 2) {
                return new InetSocketAddress(tokens[0], Integer.parseInt(tokens[1]));
            }
        }
        throw new IllegalArgumentException("Unable to parse address " + address);
    }

    private Cluster connectToCluster(final CharSequence endpoints) {
        Cluster.Builder builder = Cluster.builder();
        for (final String endpoint : ENDPOINT_PATTERN.split(endpoints)) {
            final InetSocketAddress addr = getAddress(endpoint);
            builder = builder.addContactPoint(addr.getHostName())
                             .withPort(addr.getPort());
        }

        if ((localDC != null) && !localDC.isEmpty()) {
            LOG.debug("Will use DCAwareRoundRobinPolicy, localDC={}", localDC);
            final LoadBalancingPolicy tokenAwarePolicy =
                    new TokenAwarePolicy(new DCAwareRoundRobinPolicy(localDC, numNodesToUseFromAnotherDC));
            builder.withLoadBalancingPolicy(tokenAwarePolicy);
        } else {
            LOG.debug("Don't know local DC, so use round robin + token aware + latency aware policy");
            final LoadBalancingPolicy tokenAwarePolicy =
                    new TokenAwarePolicy(new RoundRobinPolicy());
            builder.withLoadBalancingPolicy(tokenAwarePolicy);
        }

        customization.customize(builder);

        final Cluster newCluster = builder.build();
        final Metadata metadata = newCluster.getMetadata();

        LOG.debug("Connected to cassandra cluster: {}", metadata.getClusterName());
        for (final Host host : metadata.getAllHosts()) {
            LOG.debug("Cassandra node - Datacenter: {}; Host: {}; Rack: {}", host.getDatacenter(), host.getAddress(), host.getRack());
        }

        return newCluster;
    }

    private void createKeyspaceIfNeeded() {
        try {
            final String query = String.format(
                    "CREATE KEYSPACE %s WITH REPLICATION = %s",
                    QueryBuilder.quote(keyspace), replication
            );
            session.execute(query);
            LOG.debug("Keyspace '{}' successfully created", keyspace);
        } catch (final AlreadyExistsException e) {
            LOG.debug("Keyspace '{}' already exists", keyspace);
        }
    }
}
