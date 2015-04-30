package org.nohope.test;

/**
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Originally written by Elliotte Rusty Harold for the book Java I/O 2nd edition.
 * @author subwiz
 */
public class RandomInputStream extends InputStream {

    private final Random generator = new Random();
    private final int initialSize;
    private int readSize = 0;
    private boolean closed = false;

    public RandomInputStream(int initialSize) {
        this.initialSize = initialSize;
    }

    @Override
    public int read() throws IOException {
        checkOpen();
        int result = generator.nextInt() % 256;
        if (result < 0) {
            result = -result;
        }
        if (left() >= 0) {
            ++readSize;
            return result;
        }
        return -1;
    }

    @Override
    public int read(byte[] data, int offset, int length) throws IOException {
        checkOpen();
        byte[] temp = new byte[length];
        generator.nextBytes(temp);
        System.arraycopy(temp, 0, data, offset, length);
        if (left() >= 0) {
            readSize += length;
            return length;
        }
        return -1;
    }

    @Override
    public int read(byte[] data) throws IOException {
        checkOpen();
        generator.nextBytes(data);
        if (left() >= 0) {
            readSize += data.length;
            return data.length;
        }
        return -1;
    }

    @Override
    public long skip(long bytesToSkip) throws IOException {
        checkOpen();
        // It's all random so skipping has no effect.
        readSize += bytesToSkip;
        return bytesToSkip;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    private void checkOpen() throws IOException {
        if (closed) {
            throw new IOException("Input stream closed");
        }
    }

    private int left() {
        return initialSize - readSize;
    }

    @Override
    public int available() {
        return 0;
    }
}