package com.seleniumtests;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import com.seleniumtests.util.video.VideoRecorder;

public class CaptureVideoListener implements IInvokedMethodListener {
	
	private static ThreadLocal<VideoRecorder> recorders = new ThreadLocal<>();
	private static final Logger logger = LogManager.getRootLogger();

	 public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		 
		 if (method.isTestMethod()) {
			 CaptureVideo captureVideoOnMethod = method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(CaptureVideo.class);
			 CaptureVideo captureVideoOnClass = method.getTestMethod().getConstructorOrMethod().getMethod().getDeclaringClass().getAnnotation(CaptureVideo.class);
			 Class<?> clazz = method.getTestMethod().getConstructorOrMethod().getMethod().getDeclaringClass();
			 CaptureVideo captureVideoOnClass2 = null;
			 while (clazz != Object.class && captureVideoOnClass2 == null) {
				 captureVideoOnClass2 = clazz.getAnnotation(CaptureVideo.class);
				 clazz = clazz.getSuperclass();
			 }
			 
			 
			 if (captureVideoOnClass != null && captureVideoOnClass.enabled()
					|| captureVideoOnClass2 != null && captureVideoOnClass2.enabled()
					|| captureVideoOnMethod != null && captureVideoOnMethod.enabled()
			 ) {
				 VideoRecorder recorder = new VideoRecorder(new File("videos"), String.format("%s#%s.avi", 
						 method.getTestMethod().getConstructorOrMethod().getMethod().getDeclaringClass().getName(),
						 method.getTestMethod().getMethodName()));
				 logger.info("Video recording started for test");
				 recorder.start();
				 recorders.set(recorder);
			 }
		 }
	 }

	 public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
	    if (method.isTestMethod() && recorders.get() != null) {
	    	try {
				File videoFile = recorders.get().stop();
				
				// delete video file if test is OK
				if (videoFile != null && testResult.isSuccess()) {
					videoFile.delete();
				} else if (videoFile != null) {
					logger.info("Test failed => video file available at " + videoFile.getAbsolutePath());
				}
				
			} catch (IOException e) {
				logger.error("Error getting video from test " + e.getMessage());
			} finally {
				recorders.remove();
			}
	    	
	    }
	 }
}
