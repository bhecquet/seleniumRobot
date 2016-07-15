package com.seleniumtests.util.squashta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.io.IOUtils;

import com.seleniumtests.reporter.TestLogging;

/**
 * Class for generating Squash TA folder structure inside test application
 * @author behe
 *
 */
public class TaFolderStructureGenerator {
	
	private String application;
	private String sourceDir;
	private String path;
	private static final String TA_FOLDER_NAME = "squashTA";
	protected static final Logger logger = TestLogging.getLogger(TaFolderStructureGenerator.class);
	
	/**
	 * 
	 * @param application		test application name
	 * @param sourcePath		path where potential source files (java or .ta files) will be read, typically /data/<app>/squash-ta
	 * @param pathToGenerate	path where files will be generated
	 */
	public TaFolderStructureGenerator(String application, String sourcePath, String pathToGenerate) {
		this.application = application;
		this.sourceDir = sourcePath;
		this.path = pathToGenerate;
		
		new File(pathToGenerate).mkdirs();
	}

	/**
	 * Generate folder structure
	 * If java file or generic .ta file exist, they are not overwrittent
	 * @throws IOException
	 */
	public void generateDefaultStructure() throws IOException {
		File pomFile = Paths.get(path, "pom.xml").toFile();
		File javaFile = Paths.get(path, "src", TA_FOLDER_NAME, "resources", "junit", "java", "SeleniumRobotTest.java").toFile();
		File srcJavaFile = Paths.get(sourceDir, "src", TA_FOLDER_NAME, "resources", "junit", "java", "SeleniumRobotTest.java").toFile();
		File taFile = Paths.get(path, "src", TA_FOLDER_NAME, "tests", application + "_generic.ta").toFile();
		File srcTaFile = Paths.get(sourceDir, "src", TA_FOLDER_NAME, "tests", application + "_generic.ta").toFile();
		
		// add pom.xml
		logger.info("copying pom.xml to " + pomFile.toString());
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("squash-ta/pom.xml"), 
							pomFile);

		// add .java file
		if (!srcJavaFile.exists()) {
			logger.info("copying SeleniumRobotTest.java to " + javaFile.toString());
			FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("squash-ta/SeleniumRobotTest.java"), 
					javaFile);
		}
		
		// add .ta file
		if (!srcTaFile.exists()) {
			logger.info("copying generic.ta to " + taFile.toString());
			String content = IOUtils.readFully(Thread.currentThread().getContextClassLoader().getResourceAsStream("squash-ta/squash_generic.ta"));
			content = content.replace("%app%", application);
			FileUtils.writeStringToFile(taFile, content);
		}
	}
	
	/**
	 * Start generation of .ta script and folder
	 * @param args
	 * 			application		name of application
	 * 			sourceDir		root of the test application
	 * 			destDir			directory where all files are generated, Typically /data/<app>/squash-ta
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		new TaFolderStructureGenerator(args[0], Paths.get(args[1], "data", args[0], "squash-ta").toString(), args[2]).generateDefaultStructure();
		// generate new .ta files
		new TaScriptGenerator(args[0], args[1], args[2]).generateTaScripts();
	}
}
