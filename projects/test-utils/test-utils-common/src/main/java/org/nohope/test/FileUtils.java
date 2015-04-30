package org.nohope.test;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static void touch(File file) throws IOException {
        // https://stackoverflow.com/questions/1406473/simulate-touch-command-with-java
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }

        file.setLastModified(DateTime.now(DateTimeZone.UTC).getMillis());
    }

    public static String generateTemporaryFileName(String prefix) throws IOException {
        return generateTemporaryFileName(prefix, null);
    }

    public static File createTempFile(String prefix) throws IOException {
        return createTempFile(prefix, null, OnDeleteBehavior.COMMON);
    }

    public static File createTempFile(String prefix, OnDeleteBehavior behavior) throws IOException {
        return createTempFile(prefix, null, behavior);
    }

    public static File createTempFile(String prefix, String suffix, OnDeleteBehavior behavior) throws IOException {
        File tmpdir = tmpPath(prefix, suffix, behavior).getParent().toFile();
        File tempFile = File.createTempFile(prefix, suffix, tmpdir);
        //tempFile.deleteOnExit();
        return tempFile;
    }

    public static String generateTemporaryFileName(String prefix, String suffix) throws IOException {
        Path ret = tmpPath(prefix, suffix, OnDeleteBehavior.COMMON);

        return ret.toString();
    }

    public static String safeGenerateTemporaryFileName(String prefix) {
        return safeGenerateTemporaryFileName(prefix, null);
    }

    public static String safeGenerateTemporaryFileName(String prefix, String suffix) {
        try {
            return generateTemporaryFileName(prefix, suffix);
        } catch (IOException e) {
            throw new IllegalStateException("Can't create temporary file due to exception", e);
        }
    }

    public static File makeReadonlyTempDirectory(String prefix) {
        File tempdir = Paths.get(FileUtils.safeGenerateTemporaryFileName(prefix)).toFile();
        if (!tempdir.mkdirs()) {
            throw new IllegalStateException("Unable to create " + tempdir);
        }
        if (!tempdir.setReadOnly()) {
            throw new IllegalStateException("Unable make " + tempdir + " readonly");
        }
        return tempdir;
    }

    public static File createTempDir(String prefix) throws IOException {
        return createTempDir(prefix, null, OnDeleteBehavior.COMMON);
    }

    public static File createTempDir(String prefix, OnDeleteBehavior behavior) throws IOException {
        return createTempDir(prefix, null, behavior);
    }

    public static File createTempDir(String prefix, String suffix, OnDeleteBehavior behavior) throws IOException {
        File tmpdir = tmpPath(prefix, suffix, behavior).toFile();
        tmpdir.mkdirs();
        return tmpdir;
    }

    private static Path tmpPath(String prefix, String suffix, OnDeleteBehavior behavior) throws IOException {
        File f = File.createTempFile(prefix, suffix);
        f.delete();
        Path path = Paths.get(f.getAbsolutePath());
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        Path tempPath = path.getParent().resolve(FileUtils.class.getPackage().getName() + '-' + startTime);
        tempPath.toFile().mkdirs();
        behavior.register(tempPath.toFile());
        return tempPath.resolve(path.getFileName());
    }

    public static void deleteRecursively(File file) throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(file);
    }
}
