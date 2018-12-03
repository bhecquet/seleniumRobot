/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
package com.seleniumtests.util.logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.io.Files;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DoNotVisitException;

/**
 * This class aims at generating a documentation of the steps defined
 * in java sources for test app
 * It will look at java source files, select only methods defined in PageObject sub classes
 * or methods annotated with cucumber @With, @Then, @When
 * @author behe
 *
 */
public class AppStepsGenerator {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(AppStepsGenerator.class);
	
	public class TestStep {
		
		private String stepName;
		private String stepDetails;
		private String stepMethod;
		
		@Override
		public String toString() {
			return stepName;
		}

		public String getStepName() {
			return stepName;
		}

		public String getStepDetails() {
			return stepDetails;
		}

		public String getStepMethod() {
			return stepMethod;
		}

		public void setStepName(String stepName) {
			this.stepName = stepName;
		}

		public void setStepDetails(String stepDetails) {
			this.stepDetails = stepDetails;
		}

		public void setStepMethod(String stepMethod) {
			this.stepMethod = stepMethod;
		}
	}
	
	/**
	 * prevent SeleniumTestPlan sub-classes to be analyzed as they should not contain any steps
	 * @author behe
	 *
	 */
	private class ClassVisitor extends VoidVisitorAdapter<Void> {
	
		@Override
		public void visit(ClassOrInterfaceDeclaration n, Void arg) {
			if (!n.getExtendedTypes().isEmpty() && "SeleniumTestPlan".equals(n.getExtendedTypes().get(0).getName().toString())) {
				throw new DoNotVisitException("do not analyse SeleniumTestPlan sub classes");
			}	
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
        	return String.format("%s %s", annotation.getChildNodes().get(0), stepName.substring(1, stepName.lastIndexOf('\"')));
        }
        
        
		public List<TestStep> getSteps() {
			return steps;
		}
	
    }

	public static void main(String[] args) throws IOException {
		if (!new File(args[0]).isDirectory()) {
			throw new ConfigurationException(String.format("Folder %s does not exist", args[0]));
		}
		String stepsText = new AppStepsGenerator().generate(new File(args[0]));
		File stepsFile = Paths.get(args[0], "steps.txt").toFile();
		
		logger.info("writing steps to " + stepsFile);
		FileUtils.write(stepsFile, stepsText);
	}
	
	/**
	 * Returns list of java source files to analyze
	 * @return
	 */
	public List<File> getSourceFiles(File sourceDirectory) {
		if (sourceDirectory.exists() && sourceDirectory.isDirectory()) {
			return (List<File>) FileUtils.listFiles(sourceDirectory, new String[] {"java"}, true);
			    
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
		ClassVisitor clsVisitor = new ClassVisitor();
		clsVisitor.visit(cu, null);
		MethodVisitor visitor = new MethodVisitor();
		visitor.visit(cu, null);
		return visitor.getSteps();
	}
	
	public String generate(File sourceDirectory) throws IOException {
		Map<File, List<TestStep>> allSteps = new HashMap<>();
		for (File javaFile: getSourceFiles(sourceDirectory)) {
			try {
				allSteps.put(javaFile, analyzeFile(javaFile));
			} catch (DoNotVisitException e) {
				
			}
		}
		
		return formatToTxt(allSteps);
	}
	
	/**
	 * export analysis data to text
	 * @param stepsInFiles
	 * @param outputFile
	 */
	public String formatToTxt(Map<File, List<TestStep>> stepsInFiles) {
		StringBuilder out = new StringBuilder();
		for (Entry<File, List<TestStep>> entry: stepsInFiles.entrySet()) {
			out.append(entry.getKey().getName().replace(".java", "") + "\n");
			
			for (TestStep step: entry.getValue()) {
				out.append(String.format("\t%s%n", step.stepName));
				if (!step.stepDetails.isEmpty()) {
					out.append(String.format("\t\t\"%s\"%n", step.stepDetails.replace("\n", "%n\t\t")));
				}
			}
			
			out.append("------------------------------------------------------------------------------\n");
		}
		return out.toString();
	}
}
