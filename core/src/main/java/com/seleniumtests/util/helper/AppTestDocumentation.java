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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.github.javaparser.ast.Node.TreeTraversal;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.StringUtility;

/**
 * Class for creating test documentation, which can be imported into confluence through API
 * It generates a template.confluence file which contains the formatted javadoc for each Test method and step
 * https://support.atlassian.com/confluence-cloud/docs/insert-confluence-wiki-markup/
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
		
		javadoc.append("<h1>\n"
				+ "<ac:structured-macro ac:macro-id=\"723cab2f-15e1-4b30-a536-80defee1b817\" ac:name=\"toc\" ac:schema-version=\"1\"/>"
				+ "</h1>");
		
		javadoc.append("${project.summary}\n");
		
		StringBuilder pagesDoc = new StringBuilder();
		pagesDoc.append("<h1>Pages</h1>\n");
		try (Stream<Path> files = Files.walk(rootFolder)) {
			List<Path> pagesFolders = files
		        .filter(Files::isDirectory)
					.filter(p -> p.getFileName().toString().equals("webpage"))
		        .collect(Collectors.toList());
			
			for (Path pagesFolder: pagesFolders) {
				explorePages(pagesFolder.toFile(), pagesDoc);
			}
			
			
		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException("no 'webpage' sub-package found");
		}
		
		StringBuilder testDoc = new StringBuilder();
		testDoc.append("<h1>Scénarios de test</h1>\n");
		testDoc.append("<table>\n"
				+ "   <tr>\n"
				+ "       <th>Classe</th>\n" 
				+ "       <th>Test</th>\n"
				+ "       <th>Description</th>\n"
				+ "       <th>Details</th>\n"
				+ "   </tr>\n");
		try (Stream<Path> files = Files.walk(rootFolder)) {
			List<Path> testsFolders = files
					.filter(Files::isDirectory)
		        .filter(p -> p.getFileName().toString().equals("tests"))
					.collect(Collectors.toList());
			
			for (Path testsFolder: testsFolders) {
				exploreTests(testsFolder.toFile(), testDoc);
			}

		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException("no 'tests' sub-package found");
		}
		testDoc.append("</table>\n");
		
		
		javadoc.append(testDoc);

		javadoc.append("<hr/>");
		javadoc.append("${project.scmManager}\n\n");		
		
		// store usage data
		System.out.println(String.format("Number of tests: %d", tests.size()));
		System.out.println(String.format("Searched elements: %d", searchedElements));
		System.out.println(String.format("Test steps: %d", steps.size()));
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
		System.out.println(String.format("Steps reuse percentage: %.2f", usedSteps * 1.0 / stepReuse.size()));
		
		javadoc.append("<h1>Statistics</h1>\n");
		javadoc.append("<table>\n"
				+ "   <tr>\n"
				+ "       <td>Nombre de tests</td>\n" 
				+ String.format("       <td>%d</td>\n", tests.size()) 
				+ "   </tr>\n"
				+ "   <tr>\n"
				+ "       <td>Elements recherchés</td>\n" 
				+ String.format("       <td>%d</td>\n", searchedElements) 
				+ "   </tr>\n"
				+ "   <tr>\n"
				+ "       <td>Nombre de steps</td>\n" 
				+ String.format("       <td>%d</td>\n", steps.size()) 
				+ "   </tr>\n"
				+ "   <tr>\n"
				+ "       <td>Moyenne elements/steps</td>\n" 
				+ String.format("       <td>%.1f</td>\n", searchedElements * 1.0 / steps.size()) 
				+ "   </tr>\n"
				+ "   <tr>\n"
				+ "       <td>Taux de réutilisation des steps</td>\n" 
				+ String.format("       <td>%.1f</td>\n", usedSteps * 1.0 / stepReuse.size()) 
				+ "   </tr>\n"
				+ "</table>"
				);
		
		/*for (String step :steps) {
			if (!stepReuse.containsKey(step)) {
				System.out.println(step);
			}
		}
		System.out.println(new JSONObject(stepsUsedInTests).toString(2));*/
		

		FileUtils.write(Paths.get(args[0], "src/site/confluence/template.confluence").toFile(), javadoc, StandardCharsets.UTF_8);
		FileUtils.write(Paths.get(args[0], "src/site/confluence/template.html").toFile(), javadoc, StandardCharsets.UTF_8);
		
		
	}
	
	private static StringBuilder exploreTests(File srcDir, StringBuilder testDoc) throws IOException {

		try (Stream<Path> files = Files.walk(Paths.get(srcDir.getAbsolutePath()))){
		files.filter(Files::isRegularFile)
	        .filter(p -> p.getFileName().toString().endsWith(".java"))
	        .forEach(t -> {
				try {
					parseTest(t, testDoc);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		
		return testDoc;
		
	}
	
	private static void parseTest(Path path, StringBuilder testDoc) throws FileNotFoundException {
		FileInputStream in = new FileInputStream(path.toAbsolutePath().toString());

        // parse the file
		ParseResult<CompilationUnit> cu = new JavaParser().parse(in);

        // prints the resulting compilation unit to default system output
        ClassVisitor classVisitor = new ClassVisitor();
		cu.getResult().get().accept(classVisitor, "Tests");
        TestMethodVisitor methodVisitor = new TestMethodVisitor();
        cu.getResult().get().accept(methodVisitor, null);
        
        int i = 0;
        for (Entry<String, String> testEntry: methodVisitor.getMethodInfos().entrySet()) {
        	testDoc.append("<tr>\n");
        	if (i == 0) {
        		testDoc.append(String.format("    <td rowspan=\"%d\">%s</td>\n", methodVisitor.methodInfos.size(), classVisitor.getClassName()));
        	}
        	testDoc.append(String.format("    <td>%s</td>\n", testEntry.getKey().toString().split("\\.")[1])
        			+ String.format("    <td>%s</td>\n", testEntry.getValue().trim())
        	);
        	testDoc.append(String.format("    <td>%s</td>\n", String.join(", ", methodVisitor.getStepsInScenario().get(testEntry.getKey()))));
        	testDoc.append("</tr>\n");
        	i += 1;
        }
        
        stepsUsedInTests.putAll(methodVisitor.getStepsInScenario());
	}
	
	private static void explorePages(File srcDir, StringBuilder pagesDoc) throws IOException {
		try (Stream<Path> files = Files.walk(Paths.get(srcDir.getAbsolutePath()))) {
			files.filter(Files::isRegularFile)
	        .filter(p -> p.getFileName().toString().endsWith(".java"))
	        .forEach(t -> {
				try {
					parseWebPage(t, pagesDoc);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        });
		}
	}
	

	private static void parseWebPage(Path path, StringBuilder pagesDoc) throws FileNotFoundException {
		pagesDoc.append(String.format("\n<h2>Page: %s</h2>\n", path.getFileName().toString()));
		
		FileInputStream in = new FileInputStream(path.toAbsolutePath().toString());

        // parse the file
        ParseResult<CompilationUnit> cu = new JavaParser().parse(in);

        // prints the resulting compilation unit to default system output
        cu.getResult().get().accept(new ClassVisitor(), "Pages");
        WebPageMethodVisitor methodVisitor = new WebPageMethodVisitor();
		cu.getResult().get().accept(methodVisitor, null);
		
		for (Entry<String, String> pageEntry: methodVisitor.getMethodInfo().entrySet()) {
			pagesDoc.append(String.format("\n<h4>Operation: %s</h4>\n", pageEntry.getKey()));
			pagesDoc.append(pageEntry.getValue());
		}
	}
	
	private static class TestMethodVisitor extends VoidVisitorAdapter<Void> {
		
		private Map<String, String> methodInfos =  new HashMap<>();
		private Map<String, List<String>> stepsInScenario = new HashMap<>();
		
		private List<MethodCallExpr> findAllMethodCalls(Node instruction) {
			final List<MethodCallExpr> found = new ArrayList<>();
			instruction.walk(TreeTraversal.BREADTHFIRST, node -> {
	            if (MethodCallExpr.class.isAssignableFrom(node.getClass())) {
	                found.add(MethodCallExpr.class.cast(node));
	            }
	        });
	
			Collections.reverse(found);
			return found;
		}
		
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
		  
		    		stepsInScenario.put(methodId, new ArrayList<>());
		    		
		    		for (Node instruction: body.getChildNodes()) {
		    			
		    			for (MethodCallExpr methodCall: findAllMethodCalls(instruction)) {
		    				String methodName = methodCall.getNameAsString();
		    				if (steps.contains(methodName)) {
		    					stepsInScenario.get(methodId).add(methodName);
		    				}
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

    		String methodDoc = "";
	    	Optional<Comment> optComment = n.getComment();
	    	if (optComment.isPresent()) {
    			Comment comment = optComment.get();
    			methodDoc = formatJavadoc(comment.getContent());
    		} 
	    	methodInfos.put(methodId, methodDoc);
	    }

		public Map<String, String> getMethodInfos() {
			return methodInfos;
    		} 

		public Map<String, List<String>> getStepsInScenario() {
			return stepsInScenario;
	    }
	}
	
	private static class WebPageMethodVisitor extends VoidVisitorAdapter<Void> {
		
		private Map<String, String> methodInfo = new HashMap<>();
		
		@Override
		public void visit(MethodDeclaration n, Void arg) {
			
			// only display public methods
			if (!n.getModifiers().contains(Modifier.publicModifier())) {
				return;
			}		
			
			steps.add(n.getNameAsString());

			String methodJavaDoc = "";
			Optional<Comment> optComment = n.getComment();
	    	if (optComment.isPresent()) {
				Comment comment = optComment.get();
				methodJavaDoc = formatJavadoc(comment.getContent());
			}
	    	
	    	methodInfo.put(n.getNameAsString(), methodJavaDoc);
			} 

		public Map<String, String> getMethodInfo() {
			return methodInfo;
		}
	}
	
	private static class ClassVisitor extends VoidVisitorAdapter<String> {
		
		private String classDoc;
		private String className;
		
		@Override
		public void visit(ClassOrInterfaceDeclaration n, String objectType) {
			Optional<Comment> optComment = n.getComment();
			className = n.getNameAsString();
			if (optComment.isPresent()) {
				Comment comment = optComment.get();
				classDoc = String.format("%s", formatJavadoc(comment.getContent()));
			} else {
				classDoc = String.format("%s de la classe %s", objectType, n.getNameAsString());
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

		public String getClassDoc() {
			return classDoc;
		}

		public String getClassName() {
			return className;
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
				line = String.format("<pre>%s</pre>", StringUtility.encodeString(line, "html")); 
			}
			if (line.contains("@throws")) {
				continue;
			}
			out.append(line + "\n");
		}
		return out.toString();
	}
}
