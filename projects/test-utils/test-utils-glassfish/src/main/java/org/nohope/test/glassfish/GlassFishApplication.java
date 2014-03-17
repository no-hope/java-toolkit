package org.nohope.test.glassfish;

import org.glassfish.embeddable.*;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.nohope.test.glassfish.commands.Command;
import org.nohope.typetools.TStr;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
* @since 9/27/13 3:39 AM
*/
public final class GlassFishApplication {
    interface WebApp {}

    static class ScatteredWar implements WebApp {
        private final ScatteredArchive archive;

        ScatteredWar(final ScatteredArchive archive) {
            this.archive = archive;
        }

        public ScatteredArchive getArchive() {
            return archive;
        }
    }

    static class War implements WebApp {
        private final File archive;

        War(final File archive) {
            this.archive = archive;
        }

        public File getArchive() {
            return archive;
        }
    }

    private final GlassFishProperties properties;
    private final WebApp archive;
    private final GlassFish glassfish;
    private final List<Command> commands;

    GlassFishApplication(final GlassFishProperties properties, final WebApp archive, final List<Command> commands)
            throws GlassFishException {
        this.archive = archive;
        this.commands = Collections.unmodifiableList(commands);
        this.glassfish = GlassFishRuntime.bootstrap().newGlassFish(properties);
        this.properties = properties;
    }

    public void start() throws GlassFishException, IOException {
        glassfish.start();

        final CommandRunner commandRunner = glassfish.getCommandRunner();
        for (final Command command : commands) {
            commandRunner.run(command.getCommand(), command.getArgs());
        }


        final Deployer deployer = glassfish.getDeployer();
        if (archive instanceof ScatteredWar) {
            deployer.deploy(((ScatteredWar) archive).archive.toURI());
        } else if (archive instanceof War) {
            deployer.deploy(((War)archive).archive);
        } else {
            throw new IllegalStateException(TStr.format("Unknown app type: {}", archive.getClass().getCanonicalName()));
        }
    }

    public void stop() throws GlassFishException {
        for (final String name : glassfish.getDeployer().getDeployedApplications()) {
            glassfish.getDeployer().undeploy(name);
        }
        glassfish.stop();
        glassfish.dispose();
    }

    public String getBaseUrl() {
        try {
            final Collection<String> apps = glassfish.getDeployer().getDeployedApplications();
            if (apps.size() != 1) {
                throw new IllegalStateException("Unable to get context path. Either server not started or more than "
                                                + "one archive deployed");
            }

            return "http://localhost:" + properties.getPort("http-listener") + '/' + apps.iterator().next();
        } catch (final GlassFishException e) {
            throw new IllegalStateException("Server not yet started?", e);
        }
    }

    public URL getAppLocation(final String appName, final String id) throws MalformedURLException {
        return new URL(getBaseUrl() + '/' + appName + id);
    }

}
