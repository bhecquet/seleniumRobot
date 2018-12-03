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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.seleniumtests.customexception.ConfigurationException;

public class AppTestDocumentation {
	
	private static StringBuilder javadoc;
	
	public static void main(String[] args) throws IOException {
		File srcDir = Paths.get(args[0].replace(File.separator,  "/"), "src", "test", "java").toFile();
		
		javadoc = new StringBuilder("Cette page référence l'ensemble des tests et des opération disponible pour l'application\n");
		javadoc.append("\n{toc}\n\n");
		javadoc.append("${project.summary}\n");
		javadoc.append("h1. Tests\n");
		try {
			Path testsFolders = Files.walk(Paths.get(srcDir.getAbsolutePath()))
	        .filter(Files::isDirectory)
	        .filter(p -> p.getFileName().toString().equals("tests"))
	        .collect(Collectors.toList()).get(0);
			
			exploreTests(testsFolders.toFile());
		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException("no 'tests' sub-package found");
		}
		
		javadoc.append("----");
		javadoc.append("h1. Pages\n");
		try {
			Path pagesFolders = Files.walk(Paths.get(srcDir.getAbsolutePath()))
					.filter(Files::isDirectory)
					.filter(p -> p.getFileName().toString().equals("webpage"))
					.collect(Collectors.toList()).get(0);

			explorePages(pagesFolders.toFile());
		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException("no 'webpage' sub-package found");
		}

		javadoc.append("${project.scmManager}\n");
		FileUtils.write(Paths.get(args[0], "src/site/confluence/template.confluence").toFile(), javadoc, Charset.forName("UTF-8"));
	}
	
	private static void exploreTests(File srcDir) throws IOException {
		Files.walk(Paths.get(srcDir.getAbsolutePath()))
	        .filter(Files::isRegularFile)
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
	
	private static void parseTest(Path path) throws FileNotFoundException {
		javadoc.append(String.format("\nh2. Tests: %s\n", path.getFileName().toString()));
		
		FileInputStream in = new FileInputStream(path.toAbsolutePath().toString());

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);

        // prints the resulting compilation unit to default system output
        cu.accept(new ClassVisitor(), "Tests");
        cu.accept(new TestMethodVisitor(), null);
	}
	
	private static void explorePages(File srcDir) throws IOException {
		Files.walk(Paths.get(srcDir.getAbsolutePath()))
        .filter(Files::isRegularFile)
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
	

	private static void parseWebPage(Path path) throws FileNotFoundException {
		javadoc.append(String.format("\nh2. Page: %s\n", path.getFileName().toString()));
		
		FileInputStream in = new FileInputStream(path.toAbsolutePath().toString());

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);

        // prints the resulting compilation unit to default system output
        cu.accept(new ClassVisitor(), "Pages");
        cu.accept(new WebPageMethodVisitor(), null);
	}
	
	private static class TestMethodVisitor extends VoidVisitorAdapter<Void> {
		
	    public void visit(MethodDeclaration n, Void arg) {

	    	try {
	    		n.getAnnotationByClass(Test.class).get();
	    	} catch (NoSuchElementException e) {
	    		return;
	    	}

	    	javadoc.append(String.format("\nh4. Test: %s\n", n.getNameAsString()));
    		try {
    			Comment comment = n.getComment().get();
				javadoc.append(formatJavadoc(comment.getContent()));
    		} catch (NoSuchElementException e) {
    		}
	    }
	}
	
	private static class WebPageMethodVisitor extends VoidVisitorAdapter<Void> {
		
		public void visit(MethodDeclaration n, Void arg) {
			
			// only display public methods
			if (!n.getModifiers().contains(Modifier.PUBLIC)) {
				return;
			}

			javadoc.append(String.format("\nh4. Operation: %s\n", n.getNameAsString()));
			try {
				Comment comment = n.getComment().get();
				javadoc.append(formatJavadoc(comment.getContent()));
			} catch (NoSuchElementException e) {
			}
		}
	}
	
	private static class ClassVisitor extends VoidVisitorAdapter<String> {
		
		public void visit(ClassOrInterfaceDeclaration n, String objectType) {
			try {
				Comment comment = n.getComment().get();
				javadoc.append(String.format("{panel}%s{panel}\n", formatJavadoc(comment.getContent())));
			} catch (NoSuchElementException e) {
				javadoc.append(String.format("{panel}%s de la classe %s{panel}\n", objectType, n.getNameAsString()));
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
			} if (line.startsWith("@")) {
				line = String.format("{{%s}}", line); 
			}
			out.append(line + "\n");
		}
		return out.toString();
	}

//	private static final String ROOT_PACKAGE = "fr.mma.testTools.automatedTests.";
//	private static File TEST_ROOT = null; 
//	private static String TEST_ROOT_PATH = null; 
//	private static String ABSOLUTE_SRC_ROOT_PATH = null; 
//	private static String TEST_PACKAGE = null; 
//	private static String varServerUrl;
//	private static LinkedHashMap<String, TestClassInfo> classInfoList = new LinkedHashMap<String, TestClassInfo>();
//	private static Pattern varPattern = Pattern.compile("Configuration.get\\(\"(.+?)\"\\)"); 
//	
//	public static void main(String[] args) throws IOException {
//
//		String rootPath = args[0].replace(File.separator,  "/"); // le chemin vers la racine du code source
//		varServerUrl = args[1];
//		if (!varServerUrl.endsWith("/")) {
//			varServerUrl += "/";
//		}
//		
//		for (String relPath: new String[] {"/src/main/java/fr/mma/testTools/automatedTests/", "/src/test/java/fr/mma/testTools/automatedTests/"}) {
//			ABSOLUTE_SRC_ROOT_PATH = rootPath + relPath;
//
//			for (String application: getApplicationList()) {
//				classInfoList = new LinkedHashMap<String, TestClassInfo>();
//				
//				TEST_ROOT_PATH = ABSOLUTE_SRC_ROOT_PATH + application + "/tests/";
//				TEST_ROOT = new File(TEST_ROOT_PATH);
//				TEST_PACKAGE = ROOT_PACKAGE + application + ".tests.";
//			
//				System.out.println("Lecture des tests");
//				exploreTests(TEST_ROOT);
//				System.out.println("Récupération des variables associées");
//				completeVariableList();
//				System.out.println("Mise à jour des variables sur le serveur");
//				sendDoc(application);
//				System.out.println("Mise à jour des variables terminée");
//			}
//		}
//	}
//	
//	/**
//	 * Retourne la liste des applications pour lesquelles des scénarios sont définis
//	 * @return la liste des applications
//	 */
//	private static List<String> getApplicationList() {
//		List<String> applicationList = new ArrayList<String>();
//		
//		if (!new File(ABSOLUTE_SRC_ROOT_PATH).isDirectory()) {
//			return applicationList;
//		}
//		
//		for (File file: new File(ABSOLUTE_SRC_ROOT_PATH).listFiles()) {
//			
//			boolean scenarioFound = false;
//			boolean testsFound = false;
//			
//			for (File subFile: file.listFiles()) {
//				if (subFile.getName().equals("tests") && subFile.isDirectory()) {
//					testsFound = true;
//				} else if (subFile.getName().equals("navigation") && subFile.isDirectory()) {
//					scenarioFound = true;
//				}
//			}
//			
//			if (scenarioFound && testsFound) {
//				applicationList.add(file.getName());
//			}
//		}
//		return applicationList;
//	}
//	
//	/**
//	 * Construit l'arbre d'appel des méthodes permettant ensuite de récupérer les variables d'une méthode en 
//	 * particulier
//	 */
//	private static void completeVariableList() {
//		for (String className: classInfoList.keySet()) {
//			TestClassInfo testClassInfo = classInfoList.get(className);
//	
//			List<TestInfo> allTestInfos = getAllTestInfos(testClassInfo);
//			allTestInfos.addAll(testClassInfo.getTestsInfo());
//			
//			for (TestInfo testInfo: testClassInfo.getTestsInfo()) {
//				for (TestInfo testInfo2: allTestInfos) {
//		    		if (testInfo.getName().equals(testInfo2.getName())) {
//		    			continue;
//		    		}
//		    		if (testInfo.getSourceCode().contains(testInfo2.getName() + "(")) {
//		    			testInfo.getCalledMethods().add(testInfo2);
//		    		}
//		    	}
//			}
//		}
//	}
//	
//	private static List<TestInfo> getAllTestInfos(TestClassInfo testClassInfo) {
//		
//		List<TestInfo> allTestInfos = new ArrayList<TestInfo>();
//		
//		// cette classe n'a pas de parent, on ne recherche pas
//		if (testClassInfo.getExtendsClass() == null) {
//			return allTestInfos;
//		}
//		
//		// il existe un parent et celui-ci est connu, on va rechercher si il définit des variables
//		if (classInfoList.get(testClassInfo.getExtendsClass()) != null) {
//			TestClassInfo parentTestClassInfo = classInfoList.get(testClassInfo.getExtendsClass());
//			allTestInfos.addAll(parentTestClassInfo.getTestsInfo());
//			
//			// récupération des méthodes du parent
//			allTestInfos.addAll(getAllTestInfos(parentTestClassInfo));
//
//		}	
//		return allTestInfos;
//	}
//	
//	/**
//	 * navigue dans les tests de l'application
//	 * @param rootPath
//	 */	
//	private static void exploreTests(File rootPath) {
//		
//		for (File file: rootPath.listFiles()) {
//			if (file.isDirectory()) {
//				exploreTests(file);
//			} else if (file.getName().endsWith(".java")){
//				getMethods(file);
//			}
//		}
//	}
//	
//	/**
//	 * Simple visitor implementation for visiting MethodDeclaration nodes. 
//	 */
//	private static class MethodVisitor extends VoidVisitorAdapter {
//
//	    public void visit(MethodDeclaration n, Object testClassInfo) {
//
//	    	TestInfo testInfo = new TestInfo();
//	    	testInfo.setName(n.getName());
//	    	if (n.getComment() != null) {
//	    		String comment = "";
//	    		for (String line: n.getComment().getContent().trim().split("\n")) {
//	    			if (line.trim().startsWith("*")) {
//	    				line = line.trim().substring(1).trim();
//	    			}
//	    			comment += line + "\n";
//	    		}
//	    		testInfo.setDoc(comment);
//	    	}
//	    	
//	    	// analyse du code de la méthode
//	    	List<String> variables = new ArrayList<String>();
//	    	for (Statement statement: n.getBody().getStmts()) {
//	    		Matcher matcher = varPattern.matcher(statement.toString().replace("\n", ""));
//	    		
//	    		while (matcher.find()) {
//	    			String variable = matcher.group(1);
//	    			variables.add(variable);
//	    		}
//	    	}
//	    	testInfo.setVariables(variables);
//	    	testInfo.setSourceCode(n.getBody().toString());
//	    	testInfo.setTestName(((TestClassInfo)testClassInfo).getClassPath() + "." + n.getName());
//	    	
//	    	// est ce qu'on a affaire à un Test (annoté avec @Test)
//	    	if (n.getAnnotations() != null) {
//		    	for (AnnotationExpr annotation: n.getAnnotations()) {
//					if (annotation.getName().toString().equals("Test")) {
//						testInfo.setIsTest(true);
//						break;
//					}
//		    	}
//	    	}
//	    	
//	    	((TestClassInfo)testClassInfo).getTestsInfo().add(testInfo);
//	    }
//	}
//	
//	private static class ImportVisitor extends VoidVisitorAdapter {
//
//		public void visit(ImportDeclaration n, Object testClassInfo) {
//
//			if (!((TestClassInfo)testClassInfo).getImports().contains(n.toString())) {
//				((TestClassInfo)testClassInfo).getImports().add(n.toString().substring(7, n.toString().length() - 2).replace(TEST_PACKAGE, ""));
//			}
//		}
//	}
//	
//	/**
//	 * Simple visitor implementation for visiting MethodDeclaration nodes. 
//	 */
//	private static class ClassVisitor extends VoidVisitorAdapter {
//		
//		public void visit(ClassOrInterfaceDeclaration n, Object testClassInfo) {
//			
//			((TestClassInfo)testClassInfo).setName(n.getName());
//			if (n.getComment() != null) {
//				((TestClassInfo)testClassInfo).setComment(n.getComment().getContent());
//			}
//			if (n.getExtends() != null && n.getExtends().size() > 0) {
//				((TestClassInfo)testClassInfo).setExtendsClass(n.getExtends().get(0).getName());
//			}
//		}
//	}
//		
//	/**
//	 * Récupère la liste des méthodes d'une classe scénario
//	 * 
//	 * @param javaFile
//	 * @throws IOException 
//	 */
//	@SuppressWarnings("unchecked")
//	private static void getMethods(File javaFile)  {
//
//		try {
//			FileInputStream in = new FileInputStream(javaFile);
//	
//		    CompilationUnit cu;
//		    try {
//		        // parse the file
//		        cu = JavaParser.parse(in);
//		    } finally {
//		        in.close();
//		    }
//		    
//		    TestClassInfo testClassInfo = new TestClassInfo();
//		    testClassInfo.setClassPath(javaFile.getAbsolutePath().replace(File.separator,  "/").replace(TEST_ROOT_PATH, "").replace(".java", "").replace("/", "."));
//
//			// recherche toutes les méthodes dans le code source de notre classe
//		    new ImportVisitor().visit(cu, testClassInfo);
//		    new ClassVisitor().visit(cu, testClassInfo);
//		    new MethodVisitor().visit(cu, testClassInfo);
//		    
//		    classInfoList.put(testClassInfo.getClassPath(), testClassInfo);
//		    
//	
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
