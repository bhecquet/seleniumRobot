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
package com.seleniumtests.util.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.seleniumtests.customexception.ConfigurationException;

/**
 * Class for creating test documentation, which can be imported into confluence through API
 * It generates a template.confluence file which contains the formatted javadoc for each Test method and step
 * @author s047432
 *
 */
public class AppTestDocumentation {
	
	private static StringBuilder javadoc;
	private static Map<String, List<String>> stepsUsedInTests;
	private static List<String> steps;
	private static List<String> tests;
	private static Integer searchedElements;
	
	public static void main(String[] args) throws IOException {
		stepsUsedInTests = new HashMap<>();
		steps = new ArrayList<>();
		tests = new ArrayList<>();
		searchedElements = 0;
		
		File srcDir = Paths.get(args[0].replace(File.separator,  "/"), "src", "test", "java").toFile();
		
		// find the root source path (folder where "tests" and "webpage" can be found
		List<Path> rootFolders;
		try (Stream<Path> files = Files.walk(Paths.get(srcDir.getAbsolutePath()))) {
			rootFolders = files
			        .filter(Files::isDirectory)
			        .filter(p -> p.toAbsolutePath().resolve("tests").toFile().exists() && p.toAbsolutePath().resolve("webpage").toFile().exists())
			        .collect(Collectors.toList());
		}

		javadoc = new StringBuilder("Cette page référence l'ensemble des tests et des opération disponible pour l'application\n");
		
		Path rootFolder = null;
		if (rootFolders.isEmpty()) {
			System.out.println("Cannot find a folder which contains 'tests' and 'webpage' subfolder. The project does not follow conventions");
			javadoc.append("Cannot find a folder which contains 'tests' and 'webpage' subfolder. The project does not follow conventions");
			System.exit(0);
		} else {
			rootFolder = rootFolders.get(0);
		}
		
		javadoc.append("\n{toc}\n\n");
		javadoc.append("${project.summary}\n");
		javadoc.append("h1. Tests\n");
		try (Stream<Path> files = Files.walk(rootFolder)) {
			List<Path> testsFolders = files
		        .filter(Files::isDirectory)
		        .filter(p -> p.getFileName().toString().equals("tests"))
		        .collect(Collectors.toList());
			
			for (Path testsFolder: testsFolders) {
				exploreTests(testsFolder.toFile());
			}
			
			
		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException("no 'tests' sub-package found");
		}
		
		javadoc.append("----");
		javadoc.append("h1. Pages\n");
		try (Stream<Path> files = Files.walk(rootFolder)) {
			List<Path> pagesFolders = files
					.filter(Files::isDirectory)
					.filter(p -> p.getFileName().toString().equals("webpage"))
					.collect(Collectors.toList());
			
			for (Path pagesFolder: pagesFolders) {
				explorePages(pagesFolder.toFile());
			}

		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException("no 'webpage' sub-package found");
		}

		javadoc.append("${project.scmManager}\n\n");		
		
		// store usage data
		javadoc.append("h1. Statistics\n");
		javadoc.append(String.format("Number of tests: %d\n", tests.size()));
		System.out.println(String.format("Number of tests: %d", tests.size()));
		javadoc.append(String.format("Searched elements: %d\n", searchedElements));
		System.out.println(String.format("Searched elements: %d", searchedElements));
		javadoc.append(String.format("Test steps: %d\n", steps.size()));
		System.out.println(String.format("Test steps: %d", steps.size()));
		javadoc.append(String.format("Mean elements/steps: %.1f\n", searchedElements * 1.0 / steps.size()));
		System.out.println(String.format("Mean elements/steps: %.1f\n", searchedElements * 1.0 / steps.size()));
		
		int usedSteps = 0;
		Map<String, Integer> stepReuse = new HashMap<>();
		for (List<String> stepsFromTest: stepsUsedInTests.values()) {
			for (String step: stepsFromTest) {
				if (steps.contains(step)) {
					stepReuse.put(step, stepReuse.getOrDefault(step, 0) + 1);
					usedSteps++;
				}
			}
		}
		javadoc.append(String.format("Steps reuse percentage: %.1f\n", usedSteps * 1.0 / stepReuse.size()));
		System.out.println(String.format("Steps reuse percentage: %.2f", usedSteps * 1.0 / stepReuse.size()));
		
		/*for (String step :steps) {
			if (!stepReuse.containsKey(step)) {
				System.out.println(step);
			}
		}
		System.out.println(new JSONObject(stepsUsedInTests).toString(2));*/
		

		FileUtils.write(Paths.get(args[0], "src/site/confluence/template.confluence").toFile(), javadoc, StandardCharsets.UTF_8);
		
		
	}
	
	private static void exploreTests(File srcDir) throws IOException {
		try (Stream<Path> files = Files.walk(Paths.get(srcDir.getAbsolutePath()))){
		files.filter(Files::isRegularFile)
	        .filter(p -> p.getFileName().toString().endsWith(".java"))
	        .forEach(t -> {
				try {
					parseTest(t);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		
		
	}
	
	private static void parseTest(Path path) throws FileNotFoundException {
		javadoc.append(String.format("\nh2. Tests: %s\n", path.getFileName().toString()));
		
		FileInputStream in = new FileInputStream(path.toAbsolutePath().toString());

        // parse the file
        ParseResult<CompilationUnit> cu = new JavaParser().parse(in);

        // prints the resulting compilation unit to default system output
        cu.getResult().get().accept(new ClassVisitor(), "Tests");
        cu.getResult().get().accept(new TestMethodVisitor(), null);
	}
	
	private static void explorePages(File srcDir) throws IOException {
		try (Stream<Path> files = Files.walk(Paths.get(srcDir.getAbsolutePath()))) {
			files.filter(Files::isRegularFile)
	        .filter(p -> p.getFileName().toString().endsWith(".java"))
	        .forEach(t -> {
				try {
					parseWebPage(t);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        });
		}
	}
	

	private static void parseWebPage(Path path) throws FileNotFoundException {
		javadoc.append(String.format("\nh2. Page: %s\n", path.getFileName().toString()));
		
		FileInputStream in = new FileInputStream(path.toAbsolutePath().toString());

        // parse the file
        ParseResult<CompilationUnit> cu = new JavaParser().parse(in);

        // prints the resulting compilation unit to default system output
        cu.getResult().get().accept(new ClassVisitor(), "Pages");
        cu.getResult().get().accept(new WebPageMethodVisitor(), null);
	}
	
	private static class TestMethodVisitor extends VoidVisitorAdapter<Void> {
		
		@Override
	    public void visit(MethodDeclaration n, Void arg) {

	    	// read all method calls so that we can correlate with webpages
	    	String methodId;
	    	try {
	    		Optional<BlockStmt> optBody = n.getBody();
	    		Optional<Node> optParentNode = n.getParentNode();
	    		
	    		if (optBody.isPresent() && optParentNode.isPresent()) {
		    		BlockStmt body = optBody.get();
		    		
		    		methodId = ((ClassOrInterfaceDeclaration)(optParentNode.get())).getNameAsString() + "." + n.getNameAsString();
		  
		    		stepsUsedInTests.put(methodId, new ArrayList<>());
		    		
		    		for (Node instruction: body.getChildNodes()) {
		    			
		    			for (MethodCallExpr methodCall: instruction.findAll(MethodCallExpr.class)) {
		    				String methodName = methodCall.getNameAsString();
		    				if (methodName.endsWith("param") 
		    						|| methodName.equals("contains") 
		    						|| methodName.startsWith("assert")) {
		    					continue;
		    				}
		    				stepsUsedInTests.get(methodId).add(methodName);
		    			}
		    		}
		    		
	    		} else {
	    			return;
	    		}
	    	} catch (NoSuchElementException | ClassCastException e) {
	    		// we expect that 'n' is a method, and its parent is the class itself. We do not support, for example enumeration
	    		return;
	    	}
	    	
	    	
	    	// ignore non test methods
	    	Optional<AnnotationExpr> optAnnotation = n.getAnnotationByClass(Test.class);
	    	if (!optAnnotation.isPresent()) {
	    		return;
	    	}
	    	
	    	tests.add(methodId);

	    	javadoc.append(String.format("\nh4. Test: %s\n", n.getNameAsString()));
    		
	    	Optional<Comment> optComment = n.getComment();
	    	if (optComment.isPresent()) {
    			Comment comment = optComment.get();
				javadoc.append(formatJavadoc(comment.getContent()));
    		} 
	    }
	}
	
	private static class WebPageMethodVisitor extends VoidVisitorAdapter<Void> {
		
		@Override
		public void visit(MethodDeclaration n, Void arg) {
			
			// only display public methods
			if (!n.getModifiers().contains(Modifier.publicModifier())) {
				return;
			}		
			
			steps.add(n.getNameAsString());

			javadoc.append(String.format("\nh4. Operation: %s\n", n.getNameAsString()));
			
			Optional<Comment> optComment = n.getComment();
	    	if (optComment.isPresent()) {
				Comment comment = optComment.get();
				javadoc.append(formatJavadoc(comment.getContent()));
			} 
		}
	}
	
	private static class ClassVisitor extends VoidVisitorAdapter<String> {
		
		@Override
		public void visit(ClassOrInterfaceDeclaration n, String objectType) {
			Optional<Comment> optComment = n.getComment();
			if (optComment.isPresent()) {
				Comment comment = optComment.get();
				javadoc.append(String.format("{panel}%s{panel}\n", formatJavadoc(comment.getContent())));
			} else {
				javadoc.append(String.format("{panel}%s de la classe %s{panel}\n", objectType, n.getNameAsString()));
			}
			
			if ("Pages".equals(objectType)) {
				
				for (ObjectCreationExpr field: n.findAll(ObjectCreationExpr.class)) {
					if (field.getType().getNameAsString().contains("Element")) {
						searchedElements++;
					}
				}
			
				searchedElements += n.findAll(MethodCallExpr.class).stream().filter(m -> m.getNameAsString().startsWith("findElement")).collect(Collectors.toList()).size();
			}
			
		}
	}
	
	/**
	 * Remove the star in front of each line 
	 * @return
	 */
	private static String formatJavadoc(String javadoc) {
		StringBuilder out = new StringBuilder();
		for (String line: javadoc.split("\n")) {
			line = line.trim();
			if (line.startsWith("*")) {
				line = line.substring(1).trim();
			} 
			if (line.startsWith("@")) {
				line = String.format("{{%s}}", line); 
			}
			out.append(line + "\n");
		}
		return out.toString();
	}
}
