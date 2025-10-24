package com.seleniumtests.util.ide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.ParseProblemException;
import net.openhft.compiler.CompilerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumIdeParser {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumIdeParser.class);

	private final StringBuilder testCode;
	private final StringBuilder webPageCode;
	private final File javaFile;
	private final String className;
	
	public static final String PAGE_OBJECT_HEADER = """
			package com.infotel.selenium.ide;
			
			import java.io.IOException;
			
			import com.seleniumtests.uipage.PageObject;
			import org.openqa.selenium.JavascriptExecutor;
			import static org.testng.Assert.*;
			import static org.hamcrest.MatcherAssert.*;
			import static org.hamcrest.CoreMatchers.is;
			import static org.hamcrest.core.IsNot.not;
			import org.openqa.selenium.By;
			import org.openqa.selenium.Dimension;
			import org.openqa.selenium.WebElement;
			import org.openqa.selenium.interactions.Actions;
			import org.openqa.selenium.support.ui.ExpectedConditions;
			import org.openqa.selenium.support.ui.WebDriverWait;
			import org.openqa.selenium.JavascriptExecutor;
			import org.openqa.selenium.Alert;
			import org.openqa.selenium.Keys;
			import com.seleniumtests.core.TestVariable;
			import java.util.Map.Entry;
			import com.seleniumtests.util.ide.IdeHashMap;
			import java.time.Duration;
			import java.util.*;
			
			public class %sPage extends PageObject {
			
			    private Map<String, Object> vars;
			    private JavascriptExecutor js;
			
			    public %sPage() throws IOException {
			        super(null, "https://initialurl.com");
			        js = (JavascriptExecutor) driver;
			        vars = new IdeHashMap<String, Object>();
			        for (Entry<String, TestVariable> entry: robotConfig().getConfiguration().entrySet()) {
			            vars.put(entry.getKey(), entry.getValue().getValue());
			        }
			    }""";
	private static final String FOOTER = "}"; 
	
	public static final String TEST_HEADER = """
			package com.infotel.selenium.ide;
			
			import java.io.IOException;
			import com.seleniumtests.core.runner.SeleniumTestPlan;
			import org.testng.annotations.Test;
			
			public class %s extends SeleniumTestPlan {
			
			""";

	public static final String FAILED_PARSING =  """
			package com.infotel.selenium.ide;
			
			import java.io.IOException;
			import com.seleniumtests.core.runner.SeleniumTestPlan;
			import org.testng.annotations.Test;
			import org.testng.Assert;
			
			public class %s extends SeleniumTestPlan {
			
			    @Test
			    public void test%s() {
			        Assert.assertFalse(true, "%s");
			    }
			}""";
	public SeleniumIdeParser(String filePath) {
		
		javaFile = new File(filePath);
		className = javaFile.getName().replace(".java", "");
		testCode = new StringBuilder(String.format(TEST_HEADER, className));
		webPageCode = new StringBuilder(String.format(PAGE_OBJECT_HEADER, className, className));
	}
	
	/**
	 * Do some pre-computing to help execution
	 * - transform "CALL:" operation to java call
	 * - in the CALL line, replace escaped quotes by quote itself
	 */
	private String prepareJavaFile(File javaFile) {
		String initialUrl = "https://www.selenium.dev";
		boolean initialUrlFound = false;

		Pattern patternUrl = Pattern.compile("^\\s+driver.get\\(\"(.*?)\"\\);$");
		Pattern patternCall = Pattern.compile(".*System.out.println\\(\"CALL:(.*)\"\\);$");
		Pattern patternWait = Pattern.compile(".*new WebDriverWait\\(driver, (\\d+)\\);$");
		Pattern patternXPath = Pattern.compile("(.*By.xpath\\(\\\")(.*?)(\\\"\\)[,)].*)");
		Pattern patternVariableQuote = Pattern.compile("(.*?)\\\"\\s*(vars.get.*.toString\\(\\))\\\"(.*;)$"); // https://github.com/SeleniumHQ/selenium-ide/issues/1175: for files of type assertEquals(vars.get("dateAujourdhui").toString(), "vars.get("dateFin").toString()");
		Pattern patternVariable = Pattern.compile("(\\$\\{.*?})");

		try {
			StringBuilder newContent = new StringBuilder();
			String content = FileUtils.readFileToString(javaFile, StandardCharsets.UTF_8);
			for (String line: content.split("\n")) {
				line = line.replaceFirst("\\s+$", "").replace("\r", "")
						.replace("\\\\\\'", "'") // remove double escaping of single quotes
						.replace("\\'", "'"); // remove escaping of single quotes, which are not necessary

				Matcher matcherVariable = patternVariable.matcher(line);

				// replace ${someVar} by vars.get("someVar")
				while (matcherVariable.find()) {
					String variableName = matcherVariable.group(1);
					line = line.replace(variableName, String.format("vars.get(\"%s\").toString()", variableName.substring(2, variableName.length() - 1)));

				}

				Matcher matcherUrl = patternUrl.matcher(line);
				Matcher matcherCall = patternCall.matcher(line);
				Matcher matcherWait = patternWait.matcher(line);
				Matcher matcherQuote = patternVariableQuote.matcher(line);
				Matcher matcherXPath = patternXPath.matcher(line);

				// allow calling seleniumRobot code from inside a Selenium IDE script, with the use of "CALL:" comment
				if (matcherCall.matches()) {
					newContent.append(matcherCall.group(1).replace("\\\"", "\"")).append("\n");
				
				// replace WebDriverWait so that they are compatible with Selenium 4
				} else if (matcherWait.matches()) {
					newContent.append(String.format("WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(%s));\n", matcherWait.group(1)));
				
				// remove quotes around 'vars.get' when present in an assert
				// also remove escaped quotes : is("vars.get(\"stringVide\").toString()") => is(vars.get("stringVide").toString())
				} else if (matcherQuote.matches()) {
					newContent.append(String.format("%s%s%s\n", matcherQuote.group(1), matcherQuote.group(2).replace("\\\"", "\""), matcherQuote.group(3)));

				// inside a By.xpath, extract variables: By.xpath("//table//tr[contains(td[1], 'vars.get("immatriculation").toString()') ") => By.xpath("//table//tr[contains(td[1], '" + vars.get("immatriculation").toString() + "') ")
				} else if (matcherXPath.matches()) {
					String xpath = matcherXPath.group(2);
					Matcher variables = Pattern.compile("'(vars.get.*?.toString\\(\\))'").matcher(xpath);
					while (variables.find()) {
						xpath = xpath.replace(variables.group(1), "\" + " + variables.group(1) + " + \"");
					}

					Matcher variables2 = Pattern.compile("\\\\\" \\+ vars.get.*?.toString\\(\\) \\+ \\\\\"").matcher(xpath);
					while (variables2.find()) {
						xpath = xpath.replace(variables2.group(0), variables2.group(0).replace("\\\"", "\""));
					}


					newContent.append(String.format("%s%s%s\n", matcherXPath.group(1), xpath, matcherXPath.group(3)));

				// get first URL (driver.get() call) to pass it to the driver on init
				} else if (matcherUrl.matches() && !initialUrlFound) {
					initialUrl = matcherUrl.group(1);
					initialUrlFound = true;
					newContent.append(line).append("\n");

				} else {
					newContent.append(line).append("\n");
				}
			}
			
			FileUtils.writeStringToFile(javaFile, newContent.toString(), StandardCharsets.UTF_8);
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		return initialUrl;
	}
	
	public Map<String, String> parseSeleniumIdeFile() throws FileNotFoundException {
		logger.info("Reading file {}", javaFile);
		String initialUrl = prepareJavaFile(javaFile);
		Map<String, String> classInfo = new HashMap<>();
		
		// parse the file
        ParseResult<CompilationUnit> cu = new JavaParser().parse(javaFile);
		boolean codeValid = cu.isSuccessful();
		String error = cu.isSuccessful() ? null : new ParseProblemException(cu.getProblems()).getMessage().split("Problem")[0];


		Optional<CompilationUnit> cuResult = cu.getResult();

		if (codeValid && cuResult.isPresent()) {
			cuResult.get().accept(new TestMethodVisitor(), new StringBuilder[]{testCode, webPageCode});

			webPageCode.append(FOOTER);
			testCode.append(FOOTER);
			String testCodeStr = testCode
					.toString()
					.replace("new WebPage().", String.format("new %sPage().", className));

			String webPageCodeStr = webPageCode.toString()
					.replace("https://initialurl.com", initialUrl);

			logger.info("generated class {}", className);
			logger.info("\n{}", testCodeStr);
			logger.info("------------------------------------------");
			logger.info("generated class {}Page", className);
			logger.info("\n{}", webPageCodeStr);

			// try to load page code. Sometimes, parsing is done, but compilation fails
			// if compilation is done correctly, it won't be done on the next call
			try {
				CompilerUtils.CACHED_COMPILER.loadFromJava(Thread.currentThread().getContextClassLoader(), "com.infotel.selenium.ide." + className + "Page", webPageCodeStr);

				classInfo.put("com.infotel.selenium.ide." + className, testCodeStr);
				classInfo.put("com.infotel.selenium.ide." + className + "Page", webPageCodeStr);

			} catch (ClassNotFoundException e) {
				codeValid = false;
				error = e.getMessage() + " class cannot be compiled, code may be invalid. See generation logs for details";
			}
		}

		if (!codeValid) {

			logger.error("--------------------------------------------------------------------------------------------------------------------------------------------------------");
			logger.error("invalid code, one element is missing : {}", error);
			logger.error("--------------------------------------------------------------------------------------------------------------------------------------------------------");

			String testCodeStr = String.format(FAILED_PARSING, className, className, error.replace("\"", "\\\"").replace("\r", "").replace("\n", ""));
			logger.info("generated error parsing class {}", className);
			logger.info("\n{}", testCodeStr);

			classInfo.put("com.infotel.selenium.ide." + className, testCodeStr);

		}

		return classInfo;
	}
	

	private static class TestMethodVisitor extends VoidVisitorAdapter<StringBuilder[]> {
		
		@Override
	    public void visit(MethodDeclaration n, StringBuilder[] codes) {
			

			// only keep test code
	    	Optional<AnnotationExpr> beforeAnnotation = n.getAnnotationByName("Before");
	    	Optional<AnnotationExpr> afterAnnotation = n.getAnnotationByName("After");
	    	if (beforeAnnotation.isPresent() || afterAnnotation.isPresent()) {
	    		return;
	    	}
			
			StringBuilder tCode = codes[0];
			StringBuilder pageCode = codes[1];
			
			// code is always copied to PageObject
    		pageCode.append(n.getDeclarationAsString());
    		
    		Optional<BlockStmt> optBody = n.getBody();
    		if (optBody.isPresent()) {
				String body = optBody.get().toString().replace("\r", "");
				for (String line: body.split("\n")) {
					if (line.contains("System.out.println(\"STEP:") && "true".equals(System.getProperty(SeleniumTestsContext.MANUAL_TEST_STEPS))) {
						pageCode.append(line.replace("System.out.println(\"STEP:", "addStep(\"")).append("\n");
					} else if (line.contains("System.out.println(")) {
						pageCode.append(line.replace("System.out.println(", "logger.info(")).append("\n");
					} else {
			    		pageCode.append(line).append("\n");
					}
				}
	    		pageCode.append("\n\n");
	
	    		// in case of a Test method, create a reference to PageObject method
		    	Optional<AnnotationExpr> testAnnotation = n.getAnnotationByName("Test");
		    	if (testAnnotation.isPresent()) {
		    		tCode.append("    @Test\n");
		    		tCode.append(String.format("    %s throws IOException {\n", n.getDeclarationAsString()));
		    		tCode.append(String.format("        new WebPage().%s();\n", n.getNameAsString()));
		    		tCode.append("    }\n\n");
		    	} 
    		}

	    }
	}
}
