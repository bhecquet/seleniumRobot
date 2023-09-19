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

import com.seleniumtests.core.TestStepManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.ui.WebDriverWait;

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

	private StringBuilder testCode;
	private StringBuilder webPageCode;
	private File javaFile;
	private String className;
	
	public static final String PAGE_OBJECT_HEADER = "package com.infotel.selenium.ide;\n" + 
			"\n" + 
			"import java.io.IOException;\n" + 
			"\n" + 
			"import com.seleniumtests.uipage.PageObject;\n" + 
			"import org.openqa.selenium.JavascriptExecutor;\n" + 
			"import static org.testng.Assert.*;\n" + 
			"import static org.hamcrest.MatcherAssert.*;\n" + 
			"import static org.hamcrest.CoreMatchers.is;\n" + 
			"import static org.hamcrest.core.IsNot.not;\n" + 
			"import org.openqa.selenium.By;\n" + 
			"import org.openqa.selenium.Dimension;\n" + 
			"import org.openqa.selenium.WebElement;\n" + 
			"import org.openqa.selenium.interactions.Actions;\n" + 
			"import org.openqa.selenium.support.ui.ExpectedConditions;\n" + 
			"import org.openqa.selenium.support.ui.WebDriverWait;\n" + 
			"import org.openqa.selenium.JavascriptExecutor;\n" + 
			"import org.openqa.selenium.Alert;\n" + 
			"import org.openqa.selenium.Keys;\n" + 
			"import com.seleniumtests.core.TestVariable;\n" +
			"import java.util.Map.Entry;\n" +
			"import com.seleniumtests.util.ide.IdeHashMap;\n" +
			"import java.time.Duration;\n" +
			"import java.util.*;\n" +  
			"\n" + 
			"public class %sPage extends PageObject {\n" + 
			"\n" + 
			"    private Map<String, Object> vars;\n" + 	
			"    private JavascriptExecutor js;\n" + 	
			"\n" + 	
			"    public %sPage() throws IOException {\n" + 
			"        super(null, \"https://initialurl.com\");\n" +
			"        js = (JavascriptExecutor) driver;\n" + 
			"        vars = new IdeHashMap<String, Object>();\n" +
			"        for (Entry<String, TestVariable> entry: robotConfig().getConfiguration().entrySet()) {\n" + 
			"            vars.put(entry.getKey(), entry.getValue().getValue());\n" + 
			"        }\n"	+
			"    }\n";
	private static final String FOOTER = "}"; 
	
	public static final String TEST_HEADER = "package com.infotel.selenium.ide;\n" + 
			"\n" + 
			"import java.io.IOException;\n" + 
			"import com.seleniumtests.core.runner.SeleniumTestPlan;\n" +  
			"import org.testng.annotations.Test;\n" +
			"\n" + 
			"public class %s extends SeleniumTestPlan {\n\n";
	
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
		Pattern patternVariableQuote = Pattern.compile("(.*assert.*)\"(vars.get.*)\"\\);$"); // for files of type assertEquals(vars.get("dateAujourdhui").toString(), "vars.get("dateFin").toString()");
		try {
			StringBuilder newContent = new StringBuilder();
			String content = FileUtils.readFileToString(javaFile, StandardCharsets.UTF_8);
			for (String line: content.split("\n")) {
				line = line.replace("\r", "");
				Matcher matcherUrl = patternUrl.matcher(line);
				Matcher matcherCall = patternCall.matcher(line);
				Matcher matcherWait = patternWait.matcher(line);
				Matcher matcherQuote = patternVariableQuote.matcher(line);
				
				// allow calling seleniumRobot code from inside a Selenium IDE script, with the use of "CALL:" comment
				if (matcherCall.matches()) {
					newContent.append(matcherCall.group(1).replace("\\\"", "\"") + "\n");
				
				// replace WebDriverWait so that they are compatible with Selenium 4
				} else if (matcherWait.matches()) {
					newContent.append(String.format("WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(%s));\n", matcherWait.group(1)));
				
				// remove quotes around 'vars.get' when present in an assert
				} else if (matcherQuote.matches()) {
					newContent.append(String.format("%s%s);\n", matcherQuote.group(1), matcherQuote.group(2)));
					
				// get first URL (driver.get() call) to pass it the the driver on init
				} else if (matcherUrl.matches() && !initialUrlFound) {
					initialUrl = matcherUrl.group(1);
					initialUrlFound = true;
					newContent.append(line + "\n");

				} else {
					newContent.append(line + "\n");
				}
			}
			
			FileUtils.writeStringToFile(javaFile, newContent.toString(), StandardCharsets.UTF_8);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return initialUrl;
	}
	
	public Map<String, String> parseSeleniumIdeFile() throws FileNotFoundException {
		String initialUrl = prepareJavaFile(javaFile);
		Map<String, String> classInfo = new HashMap<>();
		
		// parse the file
        ParseResult<CompilationUnit> cu = new JavaParser().parse(javaFile);

        cu.getResult().get().accept(new TestMethodVisitor(), new StringBuilder[] {testCode, webPageCode});
        
        webPageCode.append(FOOTER);
        testCode.append(FOOTER);
        String testCodeStr = testCode
				.toString()
				.replace("new WebPage().", String.format("new %sPage().", className));
        
        String webPageCodeStr = webPageCode.toString()
				.replace("https://initialurl.com", initialUrl);
        
        classInfo.put("com.infotel.selenium.ide." + className, testCodeStr);
        classInfo.put("com.infotel.selenium.ide." + className + "Page", webPageCodeStr);
        
        logger.info(String.format("generated class %s", className));
        logger.info("\n" + testCodeStr);
        logger.info("------------------------------------------");
        logger.info(String.format("generated class %sPage", className));
        logger.info("\n" + webPageCodeStr);
        
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
						pageCode.append(line.replace("System.out.println(\"STEP:", "addStep(\"") + "\n");
					} else if (line.contains("System.out.println(")) {
						pageCode.append(line.replace("System.out.println(", "logger.info(") + "\n");
					} else {
			    		pageCode.append(line + "\n");
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
