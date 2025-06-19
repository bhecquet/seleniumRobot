package com.seleniumtests.util.osutility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;

import org.apache.commons.io.IOUtils;

import com.seleniumtests.util.helper.WaitHelper;

public class StreamGobber extends Thread {
    InputStream is;
    Charset charset;
    StringBuilder output;
    private boolean started = false;

    public StreamGobber(InputStream is, Charset charset)
    {
        this.is = is;
        this.charset = charset;
        output = new StringBuilder();
    }
    
    public void halt(boolean expectOutput) {

        Instant end = Instant.now().plusSeconds(3); // additional time to wait for stream to be written
        while (expectOutput && output.isEmpty() && Instant.now().isBefore(end)) {
            WaitHelper.waitForMilliSeconds(200);
        }
        started = false;
    }
    
    @Override
    public void run()
    {
    	started = true;
        try 
        {

            while (started && is != null) {
	            int isAvailable = is.available();
	        	if (isAvailable > 0) {
	        		byte[] b = new byte[isAvailable];
	        		IOUtils.read(is, b);
	        		output.append(new String(b, charset));
	        	}
	        	WaitHelper.waitForMilliSeconds(2);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }

	public StringBuilder getOutput() {
		return output;
	}
}
