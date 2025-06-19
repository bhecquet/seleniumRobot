package com.seleniumtests.ut.util.osutility;

import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.osutility.StreamGobber;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

public class TestStreamGobber {

    @Test(groups = "ut")
    public void testHalt() throws IOException {

        //connect the pipe
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        StreamGobber streamGobber = new StreamGobber(inputStream, StandardCharsets.UTF_8);
        streamGobber.start();
        outputStream.write('a');
        outputStream.flush();
        streamGobber.halt(true);
        Assert.assertEquals("a", streamGobber.getOutput().toString());
    }

    /**
     * When we expect output, we wait a bit that stream has been flushed
     * @throws IOException
     */
    @Test(groups = "ut")
    public void testHaltDelayedWrite() throws IOException {

        //connect the pipe
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        StreamGobber streamGobber = new StreamGobber(inputStream, StandardCharsets.UTF_8);
        streamGobber.start();

        // wait to write to stream
        new Thread(() -> {
            WaitHelper.waitForSeconds(2);
            try {
                outputStream.write('a');
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        streamGobber.halt(true);

        outputStream.flush();
        Assert.assertEquals("a", streamGobber.getOutput().toString());
    }

    /**
     * When we do not expect output, we do not wait a bit that stream has been flushed
     * @throws IOException
     */
    @Test(groups = "ut")
    public void testHaltDelayedWrite2() throws IOException {

        //connect the pipe
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        StreamGobber streamGobber = new StreamGobber(inputStream, StandardCharsets.UTF_8);
        streamGobber.start();
        streamGobber.halt(false);
        WaitHelper.waitForSeconds(1);
        outputStream.write('a');
        outputStream.flush();
        Assert.assertEquals("", streamGobber.getOutput().toString());
    }
}
