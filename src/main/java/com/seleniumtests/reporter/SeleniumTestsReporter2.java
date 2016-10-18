/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Copydir;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openqa.selenium.InvalidElementStateException;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.ResultMap;
import org.testng.internal.TestResult;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.SeleniumTestsPageListener;
import com.seleniumtests.core.testretry.ITestRetryAnalyzer;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.util.StringUtility;
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

public class SeleniumTestsReporter2 implements IReporter, ITestListener, IInvokedMethodListener {

	private static Logger logger = TestLogging.getLogger(SeleniumTestsReporter.class);

	private static final String RESOURCE_LOADER_PATH = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
	private static final String FAILED_TEST = "failed";
	private static final String SKIPPED_TEST = "skipped";
	private static final String PASSED_TEST = "passed";
	private static final String RESOURCES_DIR = "resources";
	//path to resources reporter directory from the root node of the project
	private static final String REPORTER_DIR = "src\\main\\resources\\reporter";

	private Map<String, Boolean> isRetryHandleNeeded = new HashMap<>();

	private Map<String, IResultMap> failedTests = new HashMap<>();

	private Map<String, IResultMap> skippedTests = new HashMap<>();

	private Map<String, IResultMap> passedTests = new HashMap<>();
	protected PrintWriter mOut;

	private String uuid = new GregorianCalendar().getTime().toString();

	private int mTreeId = 0;

	private String outputDirectory;
	private String resources;
	private JavaDocBuilder builder = null;
	private String generationErrorMessage = null;

	private File report;

	Map<String, ITestResult> methodsByGroup = null;

	protected class TestMethodSorter<T extends ITestNGMethod> implements Comparator<T> {

		/**
		 * Arrange methods by class and method name.
		 */
		@Override
		public int compare(final T o1, final T o2) {
			int r = o1.getTestClass().getName().compareTo(o2.getTestClass().getName());
			if (r == 0) {
				r = o1.getMethodName().compareTo(o2.getMethodName());
			}

			return r;
		}
	}

	protected class TestResultSorter<T extends ITestResult> implements Comparator<T> {

		/**
		 * Arrange methods by class and method name.
		 */
		@Override
		public int compare(final T o1, final T o2) {
			String sig1 = StringUtility.constructMethodSignature(o1.getMethod().getConstructorOrMethod().getMethod(), o1.getParameters());
			String sig2 = StringUtility.constructMethodSignature(o2.getMethod().getConstructorOrMethod().getMethod(), o2.getParameters());
			return sig1.compareTo(sig2);
		}
	}

	public static String escape(final String string) {
		if (null == string) {
			return string;
		}

		return string.replaceAll("\n", "<br/>");
	}

	public static void writeResourceToFile(final File file, final String resourceName, final Class<?> aClass)
			throws IOException {
		InputStream inputStream = aClass.getResourceAsStream("/" + resourceName);
		if (inputStream == null) {
			logger.error("can not find resource on the class path: " + resourceName);
		} else {

			try (
					FileOutputStream outputStream = new FileOutputStream(file);
					){
				int i;
				byte[] buffer = new byte[4096];
				while (0 < (i = inputStream.read(buffer))) {
					outputStream.write(buffer, 0, i);
				}
			} finally {
				inputStream.close();
			}
		}
	}

	private void addAllTestResults(final Set<ITestResult> testResults, final IResultMap resultMap) {
		if (resultMap != null) {
			testResults.addAll(resultMap.getAllResults());
		}
	}

	/**
	 * In case test result is SUCCESS but some softAssertions were raised, change test result to 
	 * FAILED
	 * 
	 * @param result
	 */
	public void changeTestResult(final ITestResult result) {
		List<Throwable> verificationFailures = SeleniumTestsContextManager.getThreadContext().getVerificationFailures(Reporter.getCurrentTestResult());

		int size = verificationFailures.size();
		if (size == 0) {
			return;
		} else if (result.getStatus() == TestResult.FAILURE) {
			return;
		}

		result.setStatus(TestResult.FAILURE);

		if (size == 1) {
			result.setThrowable(verificationFailures.get(0));
		} else {

			// create failure message with all failures and stack traces barring last failure)
			StringBuilder failureMessage = new StringBuilder("!!! Many Test Failures (").append(size).append(
					"):nn");
			for (int i = 0; i < size - 1; i++) {
				failureMessage.append("Failure ").append(i + 1).append(" of ").append(size).append(":n");

				Throwable t = verificationFailures.get(i);
				String fullStackTrace = Utils.stackTrace(t, false)[1];
				failureMessage.append(fullStackTrace).append("nn");
			}

			// final failure
			Throwable last = verificationFailures.get(size - 1);
			failureMessage.append("Failure ").append(size).append(" of ").append(size).append(":n");
			failureMessage.append(last.toString());

			// set merged throwable
			Throwable merged = new Throwable(failureMessage.toString());
			merged.setStackTrace(last.getStackTrace());

			result.setThrowable(merged);
		}

		// move test for passedTests to failedTests if test is not already in failed tests
		if (result.getTestContext().getPassedTests().getAllMethods().contains(result.getMethod())) {
			result.getTestContext().getPassedTests().removeResult(result);
			result.getTestContext().getFailedTests().addResult(result, result.getMethod());
		}

	}

	@Override
	public void afterInvocation(final IInvokedMethod method, final ITestResult result) {
		Reporter.setCurrentTestResult(result);

		// Handle Soft CustomAssertion
		if (method.isTestMethod()) {
			changeTestResult(result);
		}
	}

	@Override
	public void beforeInvocation(final IInvokedMethod arg0, final ITestResult arg1) { 
		//TODO
	}

	public void copyResources() throws IOException {

		File dst = new File(outputDirectory + File.separator + RESOURCES_DIR + File.separator + "templates/");
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\AdminLTE.min.css"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\bootstrap.min.css"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\bootstrap.min.js"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\Chart.min.js"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\jQuery-2.2.0.min.js"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\seleniumRobot.css"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\app.min.js"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\seleniumRobot_solo.css"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\seleniumtests_test1.gif"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\seleniumtests_test2.gif"), dst);
		FileUtils.copyFileToDirectory(new File(REPORTER_DIR + File.separator + "templates\\seleniumtests_test3.gif"), dst);
	}

	protected PrintWriter createWriter(final String outDir, final String fileName) throws IOException {
		System.setProperty("file.encoding", "UTF8");
		uuid = uuid.replaceAll(" ", "-").replaceAll(":", "-");

		File f = new File(outDir, fileName);
		logger.info("generating report " + f.getAbsolutePath());
		report = f;

		OutputStream out = new FileOutputStream(f);
		Writer writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
		return new PrintWriter(writer);

	}

	/**
	 * Completes HTML stream.
	 *
	 * @param  out
	 */
	protected void endHtml(final PrintWriter out) {
		//Add footer
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", RESOURCE_LOADER_PATH);
		try {
			ve.init();

			Template t = ve.getTemplate( "reporter/templates/report.part.test.footer.vm");
			StringWriter writer = new StringWriter();
			VelocityContext context = new VelocityContext();
			t.merge( context, writer );
			/* show the World */
			mOut.write(writer.toString());
			mOut.flush();
			mOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void executeCmd(final String browserPath, final String theUrl) {
		String cmdLine;
		String osName = System.getProperty("os.name");

		if (osName.startsWith("Windows")) {
			cmdLine = "rundll32 SHELL32.DLL,ShellExec_RunDLL " + browserPath + " " + theUrl;
		} else if (osName.startsWith("Mac")) {
			cmdLine = "open " + theUrl;
		}

		// For Linux
		else {
			cmdLine = "open " + browserPath + " " + theUrl;
		}

		try {
			Runtime.getRuntime().exec(cmdLine);
		} catch (Exception e) {
			logger.info(e);
		}
	}

	protected void generateExceptionReport(final Throwable exception, final ITestNGMethod method, final String title,
			final StringBuilder contentBuffer) {
		generateTheStackTrace(exception, method, title, contentBuffer);
	}

	public void generateExceptionReport(final Throwable exception, final ITestNGMethod method,
			final StringBuilder contentBuffer) {
		Throwable fortile = exception;
		String title = fortile.getMessage();
		if (title == null) {
			try {
				title = fortile.getCause().getMessage();
			} catch (Exception e) {
				title = e.getMessage();
			}
		}

		generateExceptionReport(exception, method, title, contentBuffer);
	}

	public void generatePanel(final VelocityEngine ve, final IResultMap map, 
			final String style, final ISuite suite, final ITestContext ctx) {

		try {
			Template t = ve.getTemplate( "reporter/templates/report.part.test.step.vm" );
			VelocityContext context = new VelocityContext();


			Collection<ITestNGMethod> methodSet = getMethodSet(map);

			for (ITestNGMethod method : methodSet) {

				
					Collection<ITestResult> resultSet = getResultSet(map, method);
					String content;
					for (ITestResult ans : resultSet) {
						StringBuilder contentBuffer = new StringBuilder();

						Object[] parameters = ans.getParameters();
						List<String> msgs = Reporter.getOutput(ans);
						boolean hasReporterOutput = !msgs.isEmpty();
						Throwable exception = ans.getThrowable();
						boolean hasThrowable = exception != null;
						if (hasReporterOutput || hasThrowable) {
							for (String line : msgs) {
								ElaborateLog logLine = new ElaborateLog(line);
								String htmllog;
								if (logLine.getHref() != null) {
									htmllog = "<li><a href='" + logLine.getHref() + "' title='" + logLine.getLocation() + "' >"
											+ logLine.getMsg() + "</a></li>";
								} else {
									htmllog = "<li>" + logLine.getMsg() + "</li>";
								}

								htmllog = htmllog.replaceAll("@@lt@@", "<").replace("^^greaterThan^^", ">");
								contentBuffer.append(htmllog);
							}
							
							if (hasThrowable) {
								generateExceptionReport(exception, method, contentBuffer);
							}
						}

						String treeId = "tree" + mTreeId;
						mTreeId++;
						context.put("dependencies", false);
						context.put("dependencies_desc", "");
						if (ans.getStatus() == 3) {
							context.put("dependencies_desc", takeCareOfDirectDependencies(suite, method, 0, ctx, treeId, ve));
							context.put("dependencies", true);
						}
						content = contentBuffer.toString();
						context.put("status", style);

						// Method description
						String javadoc = getJavadocComments(method);
						String desc = method.getDescription();
						String toDisplay = "Neither javadoc nor description for this method.";
						if (!"".equals(javadoc) && javadoc != null) {
							toDisplay = javadoc;
						} else if (!"".equals(desc) && desc != null) {
							toDisplay = desc;
						}

						// Method signature
						String methodSignature = StringUtility.constructMethodSignature(method.getConstructorOrMethod().getMethod(), parameters);
						if (methodSignature.length() > 500) {
							context.put("methodName", methodSignature.substring(0, 500) + "...");
						} else {
							context.put("methodName", methodSignature);
						}

						// Velocity values
						context.put("desc", toDisplay.replaceAll("\r\n\r\n", "<br />").replaceAll("\n\n", "<br />"));
						context.put("content", content);
						context.put("time", "Time: " + ((ans.getEndMillis() - ans.getStartMillis()) / 1000) + "sec.");
						context.put("suiteName", ctx.getName());            	       
						context.put("methods", ctx.getAllTestMethods());
						context.put("suite", suite.getAllInvokedMethods()); 
						context.put("path", REPORTER_DIR);
					//}
				}
				StringWriter writer = new StringWriter();
				t.merge( context, writer );
				// show the World 
				mOut.write(writer.toString());       
			}
		} catch (Exception e) {
			generationErrorMessage = "generatePanel, Exception creating a singleTest:" + e.getMessage();
			logger.error("Exception creating a singleTest.", e);
		}
	}

	@Override
	public void generateReport(final List<XmlSuite> xml, final List<ISuite> suites, final String outdir) {
		ITestContext testCtx = SeleniumTestsContextManager.getGlobalContext().getTestNGContext();
		if (testCtx == null) {
			logger.error("Looks like your class does not extend from SeleniumTestPlan!");
			return;
		}

		File f = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		setOutputDirectory(f.getParentFile().getAbsolutePath());
		setResources(getOutputDirectory() + "\\resources");      

		// Generate simple report
		int i = 0;
		Map<String, String> filelist = new HashMap<>();
		for (ISuite suite : suites) {
			Map<String, ISuiteResult> results = suite.getResults();
			for (ISuiteResult value : results.values()){
				try {
					i++;
					String fileName = "SeleniumTestReport-" + i + ".html";
					filelist.put(value.getTestContext().getName(), fileName);
					mOut = createWriter(getOutputDirectory(), fileName);
					startHtml(value.getTestContext(), mOut, "simple");
					generateExecutionReport(suite, value.getTestContext());
					endHtml(mOut);

					logger.info("Completed Report Generation.");

					String browserPath = SeleniumTestsContextManager.getGlobalContext().getOpenReportInBrowser();
					if (browserPath != null && browserPath.trim().length() > 0) {
						executeCmd(browserPath, getReportLocation().getAbsolutePath());
					}
				} catch (Exception e) {
					logger.error("output file", e);
				}
			}

		}

		// Generate general report
		try {

			mOut = createWriter(getOutputDirectory(), "SeleniumTestReport.html");
			startHtml(testCtx, mOut, "complete");

			// hard coded "summaryPerSuite", consider refactoring if more report configurations.
			if ("summaryPerSuite".equalsIgnoreCase(SeleniumTestsContextManager.getGlobalContext().getReportGenerationConfig())) {
				for (ISuite suite : suites) {
					List<ISuite> singleSuiteList = new ArrayList<>();
					singleSuiteList.add(suite);
					generateSuiteSummaryReport(singleSuiteList, suite.getName(), filelist);
				}
			} else {
				generateSuiteSummaryReport(suites, xml.get(0).getName(), filelist);
			}

			endHtml(mOut);
			mOut.flush();
			mOut.close();
			copyResources();
			logger.info("Completed Report Generation.");

			String browserPath = SeleniumTestsContextManager.getGlobalContext().getOpenReportInBrowser();
			if (browserPath != null && browserPath.trim().length() > 0) {
				executeCmd(browserPath, getReportLocation().getAbsolutePath());
			}
		} catch (Exception e) {
			logger.error("output file", e);
		}  
	}

	public void generateSuiteSummaryReport(final List<ISuite> suites, final String suiteName, final Map<String, String> map) {
		NumberFormat formatter = new DecimalFormat("#,##0.0");
		int quantityMethod = 0;
		int quantityPassS = 0;
		int quantitySkip = 0;
		int quantityFail = 0;
		long timeStart = Long.MAX_VALUE;
		long timeEnd = Long.MIN_VALUE;

		Map<ShortTestResult, String> tests2 = new HashMap<>();
		for (ISuite suite : suites) {
			Map<String, ISuiteResult> tests = suite.getResults();
			for (ISuiteResult r : tests.values()) {

				ITestContext overview = r.getTestContext();
				ShortTestResult mini = new ShortTestResult(overview.getName());

				int q;
				q = overview.getAllTestMethods().length;
				quantityMethod += q;
				mini.setTotalMethod(q);
				q = overview.getPassedTests().size();
				quantityPassS += q;
				mini.setInstancesPassed(q);
				q = skippedTests.get(overview.getName()).size();
				quantitySkip += q;
				mini.setInstancesSkipped(q);
				if (isRetryHandleNeeded.get(overview.getName())) {
					q = failedTests.get(overview.getName()).size();
				} else {
					q = failedTests.get(overview.getName()).size()
							+ getNbInstanceForGroup(true, overview.getFailedConfigurations());
				}
				quantityFail += q;
				mini.setInstancesFailed(q);
				
				// TODO: do not compile
				//mini.setTimeExecution((overview.getEndDate().getTime() - overview.getStartDate().getTime())/1000);
				timeStart = Math.min(overview.getStartDate().getTime(), timeStart);
				timeEnd = Math.max(overview.getEndDate().getTime(), timeEnd);
				tests2.put(mini, suite.getName());
			}
		}

		ShortTestResult total = new ShortTestResult("total");
		total.setTotalMethod(quantityMethod);
		total.setInstancesPassed(quantityPassS);
		total.setInstancesFailed(quantityFail);
		total.setInstancesSkipped(quantitySkip);

		try {
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty("resource.loader", "class");
			ve.setProperty("class.resource.loader.class", RESOURCE_LOADER_PATH);
			ve.init();

			Template t = ve.getTemplate("/reporter/templates/report.part.suiteSummary.vm");
			VelocityContext context = new VelocityContext();
			//context.put("suiteName", suiteName);
			context.put("totalRunTime", formatter.format((timeEnd - timeStart) / 1000.) + " sec");

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd MMM HH:mm:ss zzz yyyy");
			context.put("TimeStamp", simpleDateFormat.format(new GregorianCalendar().getTime()));
			context.put("tests", tests2);
			context.put("total", total);
			context.put("files", map);

			StringWriter writer = new StringWriter();
			t.merge(context, writer);
			mOut.write(writer.toString());

		} catch (Exception e) {
			generationErrorMessage = "generateSuiteSummaryReport error:" + e.getMessage();
			logger.error("generateSuiteSummaryReport error: ", e);
		}

	}

	/**
	 * Method to generate the formated stacktrace
	 * @param exception
	 * @param method
	 * @param title
	 * @param contentBuffer
	 */
	public void generateTheStackTrace(final Throwable exception, final ITestNGMethod method, final String title,
			final StringBuilder contentBuffer) {
		contentBuffer.append("<li>" + exception.getClass() + ":" + escape(title) + "(stacktrace) <br />");

		StackTraceElement[] s1 = exception.getStackTrace();
		Throwable t2 = exception.getCause();
		if (t2 == exception) {
			t2 = null;
		}
		for (int x = 0; x < s1.length; x++) {
			contentBuffer.append((x > 0 ? "<br/>at " : "") + escape(s1[x].toString()));
		}

		if (t2 != null) {
			generateExceptionReport(t2, method, "Caused by " + t2.getLocalizedMessage(), contentBuffer); // jerry
		}
		contentBuffer.append("</li>");
	}

	protected Collection<ITestNGMethod> getAllMethods(final ISuite suite) {
		Set<ITestNGMethod> all = new LinkedHashSet<>();
		Map<String, Collection<ITestNGMethod>> methods = suite.getMethodsByGroups();
		for (Entry<String, Collection<ITestNGMethod>> group : methods.entrySet()) {
			all.addAll(methods.get(group.getKey()));
		}

		return all;
	}

	protected int getDim(Class<?> cls) {
		int dim = 0;

		while (cls.isArray()) {
			dim++;
			cls = cls.getComponentType();
		}

		return dim;
	}

	protected ITestResult getFailedOrSkippedResult(final ITestContext ctx, final ITestNGMethod method) {
		List<ITestResult> res = new LinkedList<>();
		res.addAll(failedTests.get(ctx.getName()).getResults(method));
		if (!res.isEmpty()) {
			return res.get(0);
		}

		res.addAll(ctx.getPassedTests().getResults(method));
		if (!res.isEmpty()) {
			return res.get(0);
		}

		res.addAll(skippedTests.get(ctx.getName()).getResults(method));
		if (!res.isEmpty()) {
			return res.get(0);
		}

		return null;
	}


	protected JavaDocBuilder getJavaDocBuilder(final Class clz) throws URISyntaxException {
		String projectPath = new File("").getAbsolutePath();
		String packagePath = clz.getPackage().getName().replaceAll("\\.", "/");
		if (builder == null) {
			builder = new JavaDocBuilder();

			URL resource = Thread.currentThread().getContextClassLoader().getResource(packagePath);
			File src = new File(resource.toURI());
			builder.addSourceTree(src);

			// project source folder
			File realFolder = new File(projectPath + "/src/main/java/" + packagePath);
			if (realFolder.exists()) {
				builder.addSourceTree(realFolder);
			}
		}

		return builder;
	}


	/**
	 * Return formated Javadoc of the specified method
	 * @param method
	 * @return
	 */
	protected String getJavadocComments(final ITestNGMethod method) {

		try {
			Method m = method.getConstructorOrMethod().getMethod();
			String javaClass = m.getDeclaringClass().getName();
			String javaMethod = m.getName();
			JavaClass jc = getJavaDocBuilder(m.getDeclaringClass()).getClassByName(javaClass);
			Class<?>[] types = method.getConstructorOrMethod().getMethod().getParameterTypes();
			Type[] qdoxTypes = new Type[types.length];
			for (int i = 0; i < types.length; i++) {
				String type = getType(types[i]);
				int dim = getDim(types[i]);
				qdoxTypes[i] = new Type(type, dim);
			}

			JavaMethod jm = jc.getMethodBySignature(javaMethod, qdoxTypes);
			return jm.getComment();
		} catch (Exception e) {
			logger.error("Exception loading the javadoc comments for : " + method.getMethodName() + e);
			return null;
		}

	}

	/**
	 * @param   tests
	 *
	 * @return
	 */
	protected Collection<ITestNGMethod> getMethodSet(final IResultMap tests) {
		Set<ITestNGMethod> r = new TreeSet<>(new TestMethodSorter<ITestNGMethod>());
		r.addAll(tests.getAllMethods());
		return r;
	}

	protected int getNbInstanceForGroup(final boolean envt, final IResultMap tests) {
		int res = 0;

		for (ITestResult result : tests.getAllResults()) {

			boolean resultIsAnEnvtRes = Arrays.asList(result.getMethod().getGroups()).contains("envt");

			if (resultIsAnEnvtRes) {
				if (envt) {
					res++;
				}
			} else {
				if (!envt) {
					res++;
				}
			}
		}

		return res;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public File getReportLocation() {
		return report;
	}

	public String getResources() {
		return resources;
	}

	/**
	 * @param   tests
	 *
	 * @return
	 */
	protected Collection<ITestResult> getResultSet(final IResultMap tests, final ITestNGMethod method) {
		Set<ITestResult> r = new TreeSet<>(new TestResultSorter<ITestResult>());
		for (ITestResult result : tests.getAllResults()) {
			if (result.getMethod().getMethodName().equals(method.getMethodName())) {
				r.add(result);
			}
		}

		return r;
	}

	protected ITestNGMethod getTestNGMethod(final ITestContext ctx, final String method) {
		Collection<ITestNGMethod> methods = new HashSet<>();

		int index = method.substring(0, method.lastIndexOf(".")).lastIndexOf(".");
		String localMethod = method.substring(index + 1);

		ITestNGMethod[] all = ctx.getAllTestMethods();
		for (int i = 0; i < all.length; i++) {
			methods.add(all[i]);
		}

		for (ITestNGMethod m : methods) {

			if (m.toString().startsWith(localMethod)) {
				return m;
			}
		}

		throw new ScenarioException("method " + method + " not found. Should not happen. Suite " + ctx.getName());
	}

	protected String getType(Class<?> cls) {

		while (cls.isArray()) {
			cls = cls.getComponentType();
		}

		return cls.getName();
	}

	protected boolean hasDependencies(final ITestNGMethod method) {
		return (method.getGroupsDependedUpon().length + method.getMethodsDependedUpon().length) != 0;
	}

	protected Map<String, ITestResult> initMethodsByGroup() {
		methodsByGroup = new HashMap<>();

		return null;
	}

	@Override
	public void onFinish(final ITestContext arg0) {
		if (isRetryHandleNeeded.get(arg0.getName())) {
			removeIncorrectlySkippedTests(arg0, failedTests.get(arg0.getName()));
			removeFailedTestsInTestNG(arg0);
		} else {
			failedTests.put(arg0.getName(), arg0.getFailedTests());
			skippedTests.put(arg0.getName(), arg0.getSkippedTests());
			passedTests.put(arg0.getName(), arg0.getPassedTests());
			//generateExecutionReport(arg0, ve);
		}
	}

	@Override
	public void onStart(final ITestContext arg0) {
		isRetryHandleNeeded.put(arg0.getName(), false);
		failedTests.put(arg0.getName(), new ResultMap());
		skippedTests.put(arg0.getName(), new ResultMap());
		passedTests.put(arg0.getName(), new ResultMap());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(final ITestResult arg0) { 
		// overriden
	}

	/**
	 * At the end of a failed test. 
	 * Log a screenshot and retry the test.
	 * 
	 * @param argO
	 * 
	 **/
	@Override
	public synchronized void onTestFailure(final ITestResult arg0) {
		if (arg0.getMethod().getRetryAnalyzer() != null) {
			ITestRetryAnalyzer testRetryAnalyzer = (ITestRetryAnalyzer) arg0.getMethod().getRetryAnalyzer();

			if (testRetryAnalyzer.retryPeek(arg0)) {
				arg0.setStatus(ITestResult.SKIP);
				Reporter.setCurrentTestResult(null);
			} else {
				IResultMap rMap = failedTests.get(arg0.getTestContext().getName());
				rMap.addResult(arg0, arg0.getMethod());
				failedTests.put(arg0.getTestContext().getName(), rMap);
			}

			logger.info(arg0.getMethod() + " Failed in " + testRetryAnalyzer.getCount() + " times");
			isRetryHandleNeeded.put(arg0.getTestContext().getName(), true);
		}

		// capture snap shot only for the failed web tests
		// don't recreate driver if it does not exist
		if (WebUIDriver.getWebDriver(false) != null) {
			ScreenShot screenShot = new ScreenshotUtil().captureWebPageSnapshot();
			TestLogging.logWebOutput(TestLogging.buildScreenshotLog(screenShot), true);
		}
	}

	@Override
	public void onTestSkipped(final ITestResult arg0) {
		// overriden
	}

	@Override
	public void onTestStart(final ITestResult arg0) {
		// overriden
	}

	/**
	 * At the end of a successful test. 
	 * Log a screenshot.
	 * 
	 * @param argO
	 * 
	 **/
	@Override
	public void onTestSuccess(final ITestResult arg0) {
		// capture snap shot at the end of the test
		if (WebUIDriver.getWebDriver(false) != null) {
			ScreenShot screenShot = new ScreenshotUtil().captureWebPageSnapshot();
			TestLogging.logWebOutput(screenShot.getTitle()+" ("+ TestLogging.buildScreenshotLog(screenShot)+")", false);
		}
	}

	/**
	 * Remote failed test cases in TestNG.
	 *
	 * @param   tc
	 *
	 * @return
	 */
	private void removeFailedTestsInTestNG(final ITestContext tc) {
		IResultMap returnValue = tc.getFailedTests();
		ResultMap removeMap = new ResultMap();
		for (ITestResult result : returnValue.getAllResults()) {
			boolean isFailed = false;
			for (ITestResult resultToCheck : failedTests.get(tc.getName()).getAllResults()) {
				if (result.getMethod().equals(resultToCheck.getMethod())
						&& result.getEndMillis() == resultToCheck.getEndMillis()) {
					isFailed = true;
					break;
				}
			}

			if (!isFailed) {
				logger.info("Removed failed cases:" + result.getMethod().getMethodName());
				removeMap.addResult(result, result.getMethod());
			}
		}

		for (ITestResult result : removeMap.getAllResults()) {
			ITestResult removeResult = null;
			for (ITestResult resultToCheck : returnValue.getAllResults()) {
				if (result.getMethod().equals(resultToCheck.getMethod())
						&& result.getEndMillis() == resultToCheck.getEndMillis()) {
					removeResult = resultToCheck;
					break;
				}
			}

			if (removeResult != null) {
				returnValue.getAllResults().remove(removeResult);
			}
		}
	}

	/**
	 * Remove retrying failed test cases from skipped test cases.
	 *
	 * @param   tc
	 * @param   map
	 *
	 * @return
	 */
	private void removeIncorrectlySkippedTests(final ITestContext tc, final IResultMap map) {
		List<ITestNGMethod> failsToRemove = new ArrayList<>();
		IResultMap returnValue = tc.getSkippedTests();

		for (ITestResult result : returnValue.getAllResults()) {
			for (ITestResult resultToCheck : map.getAllResults()) {
				if (resultToCheck.getMethod().equals(result.getMethod())) {
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}

			for (ITestResult resultToCheck : tc.getPassedTests().getAllResults()) {
				if (resultToCheck.getMethod().equals(result.getMethod())) {
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}
		}

		for (ITestNGMethod method : failsToRemove) {
			returnValue.removeResult(method);
		}

		skippedTests.put(tc.getName(), tc.getSkippedTests());

	}

	public void setOutputDirectory(final String outtimestamped) {
		this.outputDirectory = outtimestamped;
	}

	public void setReportId(final String uuid) {
		this.uuid = uuid;
	}

	public void setResources(final String resources) {
		this.resources = resources;
	}

	/**
	 * Begin HTML stream.
	 */
	protected void startHtml(final ITestContext ctx, final PrintWriter out, final String type) {
		try {
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty("resource.loader", "class");
			ve.setProperty("class.resource.loader.class", RESOURCE_LOADER_PATH);
			ve.init();

			Template t = ve.getTemplate("/reporter/templates/report.part.header.vm");
			VelocityContext context = new VelocityContext();

			String userName = System.getProperty("user.name");
			context.put("userName", userName);
			context.put("currentDate", new Date().toString());

			DriverMode mode = SeleniumTestsContextManager.getGlobalContext().getRunMode();
			String hubUrl = SeleniumTestsContextManager.getGlobalContext().getWebDriverGrid();
			context.put("gridHub", "<a href='" + hubUrl + "' target=hub>" + hubUrl + "</a>");

			context.put("mode", mode.toString());

			StringBuilder sbGroups = new StringBuilder();
			sbGroups.append("envt,test");

			List<SeleniumTestsPageListener> pageListenerList = PluginsHelper.getInstance().getPageListeners();
			if (pageListenerList != null && !pageListenerList.isEmpty()) {
				for (SeleniumTestsPageListener abstractPageListener : pageListenerList) {
					sbGroups.append(",").append(abstractPageListener.getClass().getSimpleName());
				}
			}
			context.put("groups", sbGroups.toString());
			context.put("report", type);

			if (type == "simple"){
				context.put("header", "passed");
				if (ctx.getSkippedTests().size() >= 1) {
					context.put("header", "skipped");
				}

				if (ctx.getFailedTests().size() >= 1) {
					context.put("header", "failed");
				}
			}
			StringWriter writer = new StringWriter();
			t.merge(context, writer);
			out.write(writer.toString());

		} catch (Exception e) {
			generationErrorMessage = "startHtml error:" + e.getMessage();
			logger.error("startHtml error:", e);
		}

	}

	protected String takeCareOfDirectDependencies(final ISuite suite, final ITestNGMethod method, final int indent,
			final ITestContext ctx, final String treeId, final VelocityEngine ve) {

		try {
			Template t = ve.getTemplate( "reporter/templates/report.part.detail.vm" );
			VelocityContext context = new VelocityContext();

			//Initialisation of context values
			context.put("subDep", "");
			context.put("method", "");
			context.put("dependentGroup", "");
			context.put("indent", indent);
			String[] methStr = method.getMethodsDependedUpon();
			if (methStr.length != 0) {
				for (int i = 0; i < methStr.length; i++) {

					ITestNGMethod m = getTestNGMethod(ctx, methStr[i]);
					context.put("status", "resources/templates/seleniumtests_test"+ getFailedOrSkippedResult(ctx, m).getStatus() + ".gif");
					context.put("method", m);
					if (hasDependencies(m)) {
						context.put("subDep", takeCareOfDirectDependencies(suite, m, indent + 1, ctx, treeId, ve));
					}
				}
			}

			for (int i = 0; i < method.getGroupsDependedUpon().length; i++) {
				if (methodsByGroup == null) {
					methodsByGroup = initMethodsByGroup();
				}

				String dependentGroup = method.getGroupsDependedUpon()[i];
				context.put("dependentGroup", dependentGroup);
				Set<ITestNGMethod> methods = new LinkedHashSet<>();
				Collection<ITestNGMethod> c = suite.getMethodsByGroups().get(dependentGroup);

				if (c != null) {
					methods.addAll(c);
				}
				for (ITestNGMethod m : methods) {
					context.put("status", "resources/templates/seleniumtests_test" + getFailedOrSkippedResult(ctx, m).getStatus() + ".gif");
					context.put("method", m);
					if (hasDependencies(m)) {
						context.put("subDep", takeCareOfDirectDependencies(suite, m, indent + 1, ctx, treeId, ve));
					}
				}
			}

			StringWriter writer = new StringWriter();
			t.merge( context, writer ); 
			return writer.toString(); 

		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	public Map<String, IResultMap> getFailedTests() {
		return failedTests;
	}

	public Map<String, IResultMap> getSkippedTests() {
		return skippedTests;
	}

	public Map<String, IResultMap> getPassedTests() {
		return passedTests;
	}

	public String getGenerationErrorMessage() {
		return generationErrorMessage;
	}

	public void generateExecutionReport(ISuite suite, ITestContext value) {
		try {
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty("resource.loader", "class");
			ve.setProperty("class.resource.loader.class", RESOURCE_LOADER_PATH);
			ve.init();
			Template t = ve.getTemplate( "reporter/templates/report.part.test.vm" );
			/* create a context and add data */
			VelocityContext context = new VelocityContext();
			context.put("suiteName", value.getName());
			// Application information
			SeleniumTestsContext testContext = SeleniumTestsContextManager.getThreadContext();

			if (testContext != null) {
				String browser = testContext.getBrowser().getBrowserType();

				String app = testContext.getApp();
				String appPackage = testContext.getAppPackage();
				String appActivity = testContext.getAppActivity();
				TestType testType = testContext.getTestType();

				if (browser != null) {
					browser = browser.replace("*", "");
				}

				String browserVersion = testContext.getWebBrowserVersion();
				if (browserVersion != null) {
					browser = browser + browserVersion;
				}
				context.put("application", "");
				context.put("applicationActivity", "");
				// Log URL for web test and app info for app test
				if (testType.family().equals(TestType.WEB)) {
					context.put("applicationType", "Browser :");
					context.put("application", browser);
				} else if (testType.family().equals(TestType.APP)) {
					// Either app Or app package and app activity is specified to run test on app
					if (StringUtils.isNotBlank(app)) {
						context.put("applicationType", "App :");
						context.put("application", app);
					} else if (StringUtils.isNotBlank(appPackage)) {
						context.put("applicationType", "App Package :");
						context.put("application", appPackage);
						context.put("applicationActivity", ", App Activity : "+ appActivity); 
					}
				} else if (testType.family().equals(TestType.NON_GUI)) {
					context.put("applicationType", "");

				} else {
					context.put("applicationType", "Invalid Test type");
				}
			}   	        
			context.put("methods", value.getAllTestMethods());
			context.put("suite", suite.getAllInvokedMethods());
			context.put("status", value);

			
			StringWriter writer = new StringWriter();
			t.merge( context, writer );
			mOut.write(writer.toString());
			
			generatePanel(ve, value.getFailedTests(), FAILED_TEST, suite, value);
			generatePanel(ve, value.getSkippedTests(), SKIPPED_TEST, suite, value);
			generatePanel(ve, value.getPassedTests(), PASSED_TEST, suite, value); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

