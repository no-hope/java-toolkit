package org.nohope.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

/**
 */
public enum OnDeleteBehavior {
    
    COMMON {
        @Override
        void register(final File tmpdir) {
            registerDirectoryForDeleteOnExit(tmpdir);
        }
    },
    
    DEBUG {
        @Override
        void register(final File tmpdir) {
            LOG.debug("File " + tmpdir + " will NOT be deleted on jvm exits");
        }
    };

    
    //Logger for FileUtils is fine here. eto norma ^_^
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);
    private static final Collection<String> PATHS_TO_REMOVE = new HashSet<>();

    // .deleteOnExit doesn't work for non-empty directories, so we will use shutdown hook
    // we will use our own postponed removal only for top-level temp directory, this is very
    // useful for debugging purposes
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     synchronized (PATHS_TO_REMOVE) {
                                                         for (String path : PATHS_TO_REMOVE) {
                                                             try {
                                                                 org.apache.commons.io.FileUtils.deleteQuietly(new File(path));
                                                             } catch (Throwable e) {
                                                                 e.printStackTrace();
                                                             }
                                                         }
                                                     }
                                                 }
                                             }));
    }

    private static void registerDirectoryForDeleteOnExit(File tmpdir) {
        synchronized (PATHS_TO_REMOVE) {
            PATHS_TO_REMOVE.add(tmpdir.getAbsolutePath());
        }
    }
    
    abstract void register(File tmpdir);
}
