package com.seleniumtests.util.logging;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.util.SourceFileScanner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.io.Files;
import com.seleniumtests.customexception.ConfigurationException;

/**
 * This class aims at generating a documentation of the steps defined
 * in java sources for test app
 * It will look at java source files, select only methods defined in PageObject sub classes
 * or methods annotated with cucumber @With, @Then, @When
 * @author behe
 *
 */
public class AppStepsGenerator {
	
	public class TestStep {
		
		public String stepName;
		public String stepDetails;
		public String stepMethod;
		
		@Override
		public String toString() {
			return stepName;
		}
	}
	
	private class MethodVisitor extends VoidVisitorAdapter<Void> {
		
		private List<TestStep> steps = new ArrayList<>();
		
        @Override
        public void visit(MethodDeclaration n, Void arg) {
        	
        	TestStep step = new TestStep();
            
        	step.stepMethod = n.getName().toString();
            if (n.getJavadocComment().isPresent()) {
            	step.stepDetails = n.getJavadocComment().get().getContent().replaceAll("\\s*\\*\\s+", "\n").trim();
            } else {
            	step.stepDetails = "";
            }
            
            if (n.getAnnotationByName("Given").isPresent()) {
            	step.stepName = getAnnotationString(n.getAnnotationByName("Given").get());
            } else if (n.getAnnotationByName("When").isPresent()) {
            	step.stepName = getAnnotationString(n.getAnnotationByName("When").get());
            } else if (n.getAnnotationByName("Then").isPresent()) {
            	step.stepName = getAnnotationString(n.getAnnotationByName("Then").get());
            } else {
            	step.stepName = n.getDeclarationAsString().split("throws ")[0].trim();
            }
            
            steps.add(step);
        }
        
        private String getAnnotationString(AnnotationExpr annotation) {
        	String stepName = annotation.getChildNodes().get(1).toString();
        	return String.format("%s %s", annotation.getChildNodes().get(0), stepName.substring(1, stepName.lastIndexOf("\"")));
        }
        
        
		public List<TestStep> getSteps() {
			return steps;
		}
	
    }

	public void main(String[] args) {
		
	}
	
	/**
	 * Returns list of java source files to analyze
	 * @return
	 */
	public File[] getSourceFiles(File sourceDirectory) {
		if (sourceDirectory.exists() && sourceDirectory.isDirectory()) {
			return sourceDirectory.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".java"));
			    
		} else {
			throw new ConfigurationException(String.format("%s does not exist or is not a directory", sourceDirectory.getAbsolutePath()));
		}
	}
	
	/**
	 * Analyze the source file, looking for public method, possibly annotated 
	 * with cucumber annotations
	 * @param sourceFile
	 * @return
	 * @throws IOException 
	 */
	public List<TestStep> analyzeFile(File sourceFile) throws IOException {
		return analyzeFile(Files.asByteSource(sourceFile).openBufferedStream());
	}
	public List<TestStep> analyzeFile(InputStream inputStream) {
		CompilationUnit cu = JavaParser.parse(inputStream);
		MethodVisitor visitor = new MethodVisitor();
		visitor.visit(cu, null);
		return visitor.getSteps();
	}
	
	public String generate(File sourceDirectory) throws IOException {
		Map<File, List<TestStep>> allSteps = new HashMap<>();
		for (File javaFile: getSourceFiles(sourceDirectory)) {
			allSteps.put(javaFile, analyzeFile(javaFile));
		}
		
		return formatToTxt(allSteps);
	}
	
	/**
	 * export analysis data to text
	 * @param stepsInFiles
	 * @param outputFile
	 */
	public String formatToTxt(Map<File, List<TestStep>> stepsInFiles) {
		String out = "";
		for (Entry<File, List<TestStep>> entry: stepsInFiles.entrySet()) {
			out += entry.getKey().getName().replace(".java", "") + "\n";
			
			for (TestStep step: entry.getValue()) {
				out += String.format("\t%s\n", step.stepName);
				if (!step.stepDetails.isEmpty()) {
					out += String.format("\t\t\"%s\"\n", step.stepDetails.replace("\n", "\n\t\t"));
				}
			}
			
			out += "------------------------------------------------------------------------------\n";
		}
		return out;
	}
}
