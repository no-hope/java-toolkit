package org.nohope.validation;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/11/12 1:11 PM
 */
public class StressTool {
    private StressTool() {
    }

    public static class ProbeClass {
        public void nonnull(@Nonnull String param) {
        }

        @Nonnull
        public Object nonnull() {
            return 1;
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(final String... args) throws InterruptedException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("press a key> ");
        try {
            reader.readLine();
        } catch (final IOException ignored) {
        }

        final ProbeClass obj = new ProbeClass();

        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        obj.nonnull("");
                        obj.nonnull();
                    }
                }
            }));
        }

        for (final Thread thread : threads) {
            thread.start();
        }

        TimeUnit.MINUTES.sleep(10);
        for (final Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }
}
