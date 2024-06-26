package com.seleniumtests.util.osutility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.seleniumtests.util.helper.WaitHelper;

class StreamGobbler extends Thread {
    InputStream is;
    Charset charset;
    StringBuilder output;
    private boolean started = false;
    
    StreamGobbler(InputStream is, Charset charset)
    {
        this.is = is;
        this.charset = charset;
        output = new StringBuilder();
    }
    
    public void halt() {
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
