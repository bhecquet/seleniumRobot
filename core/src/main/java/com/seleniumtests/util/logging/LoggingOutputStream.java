package com.seleniumtests.util.logging;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;


/**
 * Code from https://sysgears.com/articles/how-to-redirect-stdout-and-stderr-writing-to-a-log4j-appender/
 * @author s047432
 *
 */
public abstract class LoggingOutputStream extends OutputStream {

    /**
     * Default number of bytes in the buffer.
     */
    private static final int DEFAULT_BUFFER_LENGTH = 2048;

    /**
     * Indicates stream state.
     */
    private boolean hasBeenClosed = false;

    /**
     * Internal buffer where data is stored.
     */
    private byte[] buf;

    /**
     * The number of valid bytes in the buffer.
     */
    private int count;

    /**
     * Remembers the size of the buffer.
     */
    private int curBufLength;

    /**
     * The logger to write to.
     */
    protected Logger logger;

    /**
     * The log level.
     */
    protected Level level;

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param logger         the Logger to write to
     * @param level       the log level
     * @throws IllegalArgumentException in case if one of arguments is  null.
     */
    public LoggingOutputStream(final Logger logger,
                               final Level level)
            throws IllegalArgumentException {
        if (logger == null || level == null) {
            throw new IllegalArgumentException(
                    "Logger or log level must be not null");
        }
        this.logger = logger;
        this.level = level;
        curBufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[curBufLength];
        count = 0;
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs.
     */
    public void write(final int b) throws IOException {
        if (hasBeenClosed) {
            throw new IOException("The stream has been closed.");
        }
        // don't log nulls
        if (b == 0) {
            return;
        }
        // would this be writing past the buffer?
        if (count == curBufLength) {
            // grow the buffer
            final int newBufLength = curBufLength +
                    DEFAULT_BUFFER_LENGTH;
            final byte[] newBuf = new byte[newBufLength];
            System.arraycopy(buf, 0, newBuf, 0, curBufLength);
            buf = newBuf;
            curBufLength = newBufLength;
        }

        buf[count] = (byte) b;
        count++;
    }

    /**
     * Flushes this output stream and forces any buffered output
     * bytes to be written out.
     */
    @Override
    public void flush() {
        if (count == 0) {
            return;
        }
        final byte[] bytes = new byte[count];
        System.arraycopy(buf, 0, bytes, 0, count);
        String str = new String(bytes);
        str = str.replaceAll("\\r\\n$", "").replaceAll("\\n$", "");
        if (!str.isEmpty()) {
        	log(str);
        }
        count = 0;
    }
    
    protected abstract void log(String str);

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
     */
    @Override
    public void close() {
        flush();
        hasBeenClosed = true;
    }
}
