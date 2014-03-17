package org.nohope.test.glassfish;

import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.nohope.test.SocketUtils;
import org.nohope.test.glassfish.commands.Command;
import org.nohope.typetools.TStr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* Date: 2/11/14
* Time: 1:34 PM
*/
public class GlassfishAppBuilder {
    private int port;
    private final GlassFishApplication.WebApp war;
    private final List<Command> commands = new ArrayList<>();


    GlassfishAppBuilder(final GlassFishApplication.WebApp app) {
        this.port = SocketUtils.getAvailablePort();
        this.war = app;
    }

    public GlassfishAppBuilder(final File war) {
        this(new GlassFishApplication.War(war));
    }

    public GlassfishAppBuilder(final String archiveName) {
        this(new GlassFishApplication.ScatteredWar(new ScatteredArchive(archiveName, ScatteredArchive.Type.WAR)));
    }

    public GlassfishAppBuilder setPort(final int port) {
        this.port = port;
        return this;
    }

    public GlassfishAppBuilder addCommand(final String command, final String... args) {
        commands.add(new Command(command, args));
        return this;
    }

    public GlassfishAppBuilder addCommands(final List<Command> command) {
        commands.addAll(command);
        return this;
    }


    public GlassfishAppBuilder addMetadata(final String path) {
        checkAppType();
        addMetadata(path, null);
        return this;
    }

    private void checkAppType() {
        if (!(war instanceof GlassFishApplication.ScatteredWar)) {
            throw new IllegalStateException("This method may only be called for Scattered Archive!");
        }
    }

    public GlassfishAppBuilder addMetadata(final String path, final String name) {
        checkAppType();
        final File file = new File(path);
        if (!file.exists()) {
            throw new IllegalStateException(TStr.format("File does not exist: {}", file.getAbsolutePath()));
        }

        final ScatteredArchive arch = ((GlassFishApplication.ScatteredWar) war).getArchive();

        try {
            if (name == null) {
                arch.addMetadata(file);
            } else {
                arch.addMetadata(file, name);
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }

        return this;
    }

    public GlassfishAppBuilder addClassPath(final String path) {
        checkAppType();
        try {
            ((GlassFishApplication.ScatteredWar) war).getArchive().addClassPath(new File(path));
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    public GlassFishApplication build() {
        final GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", port);

        try {
            return new GlassFishApplication(props, war, commands);
        } catch (final GlassFishException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
