package org.nohope.test.stress.result;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.nohope.test.stress.result.StressScenarioResult.Interpreter;
import org.nohope.test.stress.result.metrics.GcMetrics;
import org.nohope.test.stress.result.simplified.SimpleInterpreter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 */
public class ExportingInterpreter implements Interpreter<Path> {

    private final Path targetDirectory;
    private static final DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd--hh-mm-ss");
    private final String suffix;

    public ExportingInterpreter(final Path targetDirectory, final String suffix) {
        this.targetDirectory = targetDirectory;
        this.suffix = suffix;
        final File file = targetDirectory.toFile();

        if (!file.exists()) {
            file.mkdirs();
        }

        if (!file.isDirectory()) {
            throw new IllegalStateException(String.format("Not a directory: %s", targetDirectory.toString()));
        }
    }

    @Override
    public Path interpret(final StressScenarioResult result) {
        try {
            final String filename = String.format("stresstest-%s-%s-%s.zip",
                                                  getHostName(),
                                                  suffix,
                                                  DateTime.now().toString(format));
            final Path targetName = targetDirectory.resolve(filename);
            final File targetFile = targetName.toFile();
            targetFile.delete();

            try (final FileOutputStream fos = new FileOutputStream(targetFile);
                 final ZipOutputStream zos = new ZipOutputStream(fos)) {
                newZipEntry("metadata.txt", zos, result, ExportingInterpreter::writeMetadata);
                newZipEntry("calls_successful.txt", zos, result, ExportingInterpreter::writeSuccessfulOperations);
                newZipEntry("calls_failed.txt", zos, result, ExportingInterpreter::writeFailedOperations);
                newZipEntry("test_metrics.txt", zos, result, ExportingInterpreter::writeMetrics);
                newZipEntry("test_summary.txt", zos, result, ExportingInterpreter::writeSummary);
                newZipEntry("stacktraces.txt", zos, result, ExportingInterpreter::writeStackTraces);
                writeSeparateStatistics(zos, result);

                zos.closeEntry();
            }

            return targetName;
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String getHostName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    private static void writeMetadata(final ZipOutputStream zipOutputStream, final StressScenarioResult stressScenarioResult) {
        try {
            zipOutputStream.write("format: 2015-04-29\n".getBytes(Charsets.UTF_8));
            zipOutputStream.write(String.format("host: %s\n", getHostName()).getBytes(Charsets.UTF_8));
            zipOutputStream.write(String.format("user: %s\n", System.getProperty("user.name")).getBytes(Charsets.UTF_8));
            zipOutputStream.write(String.format("cpus: %d\n", Runtime.getRuntime().availableProcessors()).getBytes(Charsets.UTF_8));
            zipOutputStream.write(String.format("heapmax: %d\n", Runtime.getRuntime().maxMemory()).getBytes(Charsets.UTF_8));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeSummary(final ZipOutputStream zos, final StressScenarioResult result) {
        final String summary = new SimpleInterpreter().interpret(result).toString();
        try {
            zos.write(summary.getBytes());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeSuccessfulOperations(final ZipOutputStream zos, final StressScenarioResult result) {
        result.visitResult((name, threadId, startNanos, endNanos) -> {
            try {
                writeLine(zos, name, threadId, startNanos, endNanos, endNanos - startNanos);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static void writeSeparateStatistics(final ZipOutputStream zos, final StressScenarioResult result) {
        final AtomicReference<String> currentName = new AtomicReference<>(null);

        result.visitResult((name, threadId, startNanos, endNanos) -> {
            try {
                final String cname = currentName.get();
                if (!name.equals(cname)) {
                    final ZipEntry zipEntry = new ZipEntry(
                            String.format("%s/calls_successful/%s.txt", getHostName(), name));
                    zos.putNextEntry(zipEntry);
                    currentName.set(name);
                }
                writeLine(zos, name, threadId, startNanos, endNanos, endNanos - startNanos);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });

        currentName.set(null);

        result.visitErrors((name, threadId, e, startNanos, endNanos) -> {
            try {
                final String cname = currentName.get();
                if (!name.equals(cname)) {
                    final ZipEntry zipEntry = new ZipEntry(String.format("%s/calls_failed/%s.txt", getHostName(), name));
                    zos.putNextEntry(zipEntry);
                    currentName.set(name);
                }

                writeLine(zos, name, threadId, startNanos, endNanos, endNanos - startNanos,
                          e.getClass().getCanonicalName());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });

    }

    private static void writeFailedOperations(final ZipOutputStream zos, final StressScenarioResult result) {
        result.visitErrors((name, threadId, e, startNanos, endNanos) -> {
            try {
                writeLine(zos, name, threadId, startNanos, endNanos, endNanos - startNanos,
                          e.getClass().getCanonicalName());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private static void writeStackTraces(final ZipOutputStream zos, final StressScenarioResult result) {
        result.visitErrors((name, threadId, e, startNanos, endNanos) -> {
            try {
                writeLine(zos, name, threadId, startNanos, endNanos, endNanos - startNanos,
                          e.getClass().getCanonicalName());
                zos.write(ExceptionUtils.getStackTrace(e).getBytes());
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private static void writeMetrics(final ZipOutputStream zos, final StressScenarioResult result) {
        result.visitMetrics(metric -> {
            try {
                final Object[] values = {
                        metric.getTimestampNanos()
                        , "PCL", metric.getProcessMetrics().getProcessCpuLoad()
                        , "PCT", metric.getProcessMetrics().getProcessCpuTime()
                        , "SCT", metric.getSystemMetrics().getSystemCpuTime()
                        , "SLA", metric.getSystemMetrics().getSystemLoadAverage()
                };

                final List<String> gc = metric.getGcMetrics().entrySet().stream()
                                              .flatMap(ExportingInterpreter::gcStat)
                                              .collect(Collectors.toList());

                writeLine(zos, ArrayUtils.addAll(values, gc.toArray()));
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private static Stream<String> gcStat(final Map.Entry<String, GcMetrics> metrics) {
        final Collection<String> ret = new ArrayList<>();
        ret.add(metrics.getKey()+":CC");
        ret.add(Long.valueOf(metrics.getValue().getCollectionCount()).toString());
        ret.add(metrics.getKey()+":CT");
        ret.add(Long.valueOf(metrics.getValue().getCollectionTime()).toString());
        //ret.add(metrics.getKey() + ":GCI");
        //ret.add(metrics.getValue().getGcInfo().toString());
        return ret.stream();
    }

    private static void newZipEntry(final String name,
                                    final ZipOutputStream zos,
                                    final StressScenarioResult result,
                                    final BiConsumer<ZipOutputStream, StressScenarioResult> writer) throws IOException {
        final ZipEntry zipEntry = new ZipEntry(String.format("%s/%s", getHostName(), name));
        zos.putNextEntry(zipEntry);
        writer.accept(zos, result);
    }

    private static void writeLine(final ZipOutputStream zos, final Object... values) throws IOException {
        for (final Object value : values) {
            zos.write(value.toString().getBytes(Charsets.UTF_8));
            zos.write(';');
        }
        zos.write('\n');

    }
}
