package org.nohope.archiva;

import org.apache.archiva.admin.model.beans.ManagedRepository;
import org.apache.archiva.consumers.AbstractMonitoredConsumer;
import org.apache.archiva.consumers.ConsumerException;
import org.apache.archiva.consumers.KnownRepositoryContentConsumer;
import org.apache.commons.io.Charsets;
import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Generate OSGi Bundle Repository XML metadata out of OSGi bundle resources.
 * This requires adding the Apache Felix BundleRepository and OSGi Core JARs to
 * Archiva's classpath. This can be accomplished by adding those JARs to the
 * {@code WEB-INF/lib} directory of the web application.
 * <p>
 * By default the OBR metadata will be created as a file named
 * {@code metadata.xml} at the root of the Archiva repository. The directory can
 * be changed by adding a {@code $appserver.base/conf/obr.properties} file with
 * a <b>obr.base</b> key with an associated absolute file path to the
 * desired directory. Note that {@code $appserver.base} represents the
 * {@code appserver.base} System property, which is normally set by the Archiva
 * start up script.
 *
 * @author matt
 * @version 0.1
 */
@Service("knownRepositoryContentConsumer#create-obr-metadata")
@Scope("prototype")
public class OBRMetadataRepositoryConsumer extends AbstractMonitoredConsumer implements KnownRepositoryContentConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(OBRMetadataRepositoryConsumer.class);

    private static final String ID = "create-obr-metadata";
    private static final String DESC = "Create OSGi Bundle Repository metadata from artifacts in the repository.";
    private String repoId;
    private Properties props;
    private String archivaRepositoryLocation;
    private File metadataFile;
    private RepositoryImpl obrRepository;
    private long obrRepositoryLastModifiedDate;
    private long foundMostRecentModifiedDate;
    private DataModelHelper dataModelHelper;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public List<String> getIncludes() {
        // we only care about OSGi bundle artifacts
        return Collections.singletonList("**/*.jar");
    }

    @Override
    public List<String> getExcludes() {
        // we also want to exclude searching in the default locations, and exclude source JARs
        List<String> excludes = new ArrayList<>(getDefaultArtifactExclusions());
        excludes.add("**/*-sources.jar");
        return excludes;
    }

    @Override
    public void beginScan(ManagedRepository repository, Date whenGathered) throws ConsumerException {
        beginScan(repository, whenGathered, true);
    }

    private void loadProperties() {
        File propFile = new File(System.getProperty("appserver.base", "."), "conf/obr.properties");
        props = new Properties();
        if (propFile.canRead()) {
            try (final InputStream in = new BufferedInputStream(new FileInputStream(propFile))) {
                props.load(in);
            } catch (IOException e) {
                LOG.warn("Unable to load OBR properties from {}: {}", propFile, e.getMessage());
            }
        }
    }

    @Override
    public void beginScan(ManagedRepository repository, Date whenGathered, boolean executeOnEntireRepo) throws ConsumerException {
        repoId = repository.getId();
        LOG.info("Beginning scan on repository {} on {}; all = {}", repoId, whenGathered, executeOnEntireRepo);
        loadProperties();
        archivaRepositoryLocation = repository.getLocation();

        final String basePath = props.getProperty("obr.base", archivaRepositoryLocation);
        obrRepositoryLastModifiedDate = 0;
        foundMostRecentModifiedDate = 0;
        metadataFile = new File(basePath, "metadata.xml");
        if (!metadataFile.getParentFile().isDirectory() && !metadataFile.getParentFile().mkdirs()) {
            LOG.error("OBR base path {} does not exist and can't be created.", basePath);
            throw new ConsumerException("OBR base path does not exist: " + basePath);
        }
        dataModelHelper = new DataModelHelperImpl();
        if (metadataFile.canRead()) {
            // load up existing repository
            try (final Reader reader = new InputStreamReader(new FileInputStream(metadataFile), Charsets.UTF_8)) {
                Repository existingRepo = dataModelHelper.readRepository(reader);
                if (!executeOnEntireRepo) {
                    if (existingRepo != null) {
                        obrRepositoryLastModifiedDate = existingRepo.getLastModified();
                        LOG.info("OBR metadata last generated at {}", new Date(obrRepositoryLastModifiedDate));
                    }
                    obrRepository = (RepositoryImpl) existingRepo;
                }
            } catch (Exception e) {
                LOG.warn("Exception reading existing OBR metadata; will recreate", e);
            }
        }
        if (obrRepository == null) {
            obrRepository = new RepositoryImpl();
        }
    }

    @Override
    public void processFile(String path) throws ConsumerException {
        LOG.debug("Processing file {}", path);
        try {
            File file = new File(archivaRepositoryLocation, path);
            final long foundLastModified = file.lastModified();
            if (foundLastModified > obrRepositoryLastModifiedDate) {
                ResourceImpl obrResource = (ResourceImpl) dataModelHelper.createResource(file.toURI().toURL());
                // we want a relative URL, so override the absolute path ResourceImpl generated
                obrResource.put(Resource.URI, path);
                obrRepository.addResource(obrResource);
                if (foundLastModified > foundMostRecentModifiedDate) {
                    foundMostRecentModifiedDate = foundLastModified;
                }
            } else if (LOG.isTraceEnabled()) {
                LOG.trace("Skipping unmodified OBR resource {}", path);
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("Skipping OBR resource {}, does not appear to be a bundle", path);
        } catch (IOException e) {
            throw new ConsumerException("Error creating OBR resource", e);
        }
    }

    @Override
    public void processFile(String path, boolean executeOnEntireRepo) throws Exception {
        processFile(path);
    }

    @Override
    public void completeScan() {
        LOG.debug("Completed scan of repository {}", repoId);
        if (metadataFile.exists() && foundMostRecentModifiedDate <= obrRepositoryLastModifiedDate) {
            LOG.info("OBR metadata unchanged.");
            return;
        }
        // write the XML to a temporary file first, and if that succeeds then move the temporary
        // file to the final destination
        File tmpFile = new File(metadataFile.getAbsolutePath() + ".tmp");
        try (final Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(metadataFile), Charsets.UTF_8))) {
            dataModelHelper.writeRepository(obrRepository, writer);
            writer.flush();
            if (metadataFile.exists() && !metadataFile.delete()) {
                LOG.warn("Unable to delete old OBR metadata file {}", metadataFile);
            }
            if (!tmpFile.renameTo(metadataFile)) {
                LOG.warn("Unable to move temporary OBR metadata file to {}", metadataFile);
            } else if (LOG.isInfoEnabled()) {
                LOG.info("Created OBR metadata {}", metadataFile);
            }
        } catch (IOException e) {
            LOG.error("Error writing OBR metadata file {}", metadataFile, e);
        }
    }

    @Override
    public void completeScan(boolean executeOnEntireRepo) {
        completeScan();
    }
}
