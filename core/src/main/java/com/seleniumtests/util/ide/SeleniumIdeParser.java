package com.seleniumtests.util.ide;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
			"import java.util.*;\n" +  
			"\n" + 
			"public class %sPage extends PageObject {\n" + 
			"\n" + 
			"    private Map<String, Object> vars;\n" + 	
			"    private JavascriptExecutor js;\n" + 	
			"\n" + 	
			"    public %sPage() throws IOException {\n" + 
			"        super();\n" + 
			"        js = (JavascriptExecutor) driver;\n" + 
			"        vars = new HashMap<String, Object>();\n" + 
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
	
	public Map<String, String> parseSeleniumIdeFile() throws FileNotFoundException {
		Map<String, String> classInfo = new HashMap<>();
		
		// parse the file
        ParseResult<CompilationUnit> cu = new JavaParser().parse(javaFile);

        cu.getResult().get().accept(new TestMethodVisitor(), new StringBuilder[] {testCode, webPageCode});
        
        webPageCode.append(FOOTER);
        testCode.append(FOOTER);
        
        classInfo.put("com.infotel.selenium.ide." + className, testCode.toString().replace("new WebPage().", String.format("new %sPage().", className)));
        classInfo.put("com.infotel.selenium.ide." + className + "Page", webPageCode.toString());
        
        logger.info(String.format("generated class %s", className));
        logger.info("\n" + testCode.toString());
        logger.info("------------------------------------------");
        logger.info(String.format("generated class %sPage", className));
        logger.info("\n" + webPageCode.toString());
        
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
	    	
	    	// TODO: steps
	    }
	}
}
