package ca.uwaterloo.averroes.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import ca.uwaterloo.averroes.exceptions.Assertions;
import ca.uwaterloo.averroes.exceptions.AverroesException;

/**
 * A utility class that holds all the properties required by Averroes to run. For the possible values of each property,
 * you can consult the accompanying properties/averroes.properties.sample file or the online tutorial at {@link http
 * ://plg.uwaterloo.ca/~karim/projects/averroes/tutorial.php}
 * 
 * @author karim
 * 
 */
public final class AverroesProperties {

	public static final String PROPERTY_FILENAME = "averroes.properties";

	public static final String APPLICATION_INCLUDES = "application_includes";
	public static final String MAIN_CLASS = "main_class";
	public static final String INPUT_JAR_FILES = "input_jar_files";
	public static final String LIBRARY_JAR_FILES = "library_jar_files";
	public static final String DYNAMIC_CLASSES_FILE = "dynamic_classes_file";
	public static final String TAMIFLEX_FACTS_FILE = "tamiflex_facts_file";

	public static final String OUTPUT_DIR = "output_dir";

	public static final String JRE = "jre";

	private static Properties properties = null;
	private static List<String> dynamicClasses = null;

	/**
	 * Load the properties file at the first access of this class.
	 */
	static {
		try {
			loadProperties();
		} catch (AverroesException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Load the properties file.
	 * 
	 * @throws AverroesException
	 */
	private static void loadProperties() throws AverroesException {
		System.out.println("Loading properties.");
		try {
			properties = loadPropertiesFromFile(AverroesProperties.class.getClassLoader(), PROPERTY_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AverroesException("Unable to load Averroes properties.", e);
		}
	}

	/**
	 * Load the properties from a file.
	 * 
	 * @param loader
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static Properties loadPropertiesFromFile(ClassLoader loader, String fileName) throws IOException {
		if (loader == null) {
			throw new IllegalArgumentException("loader is null");
		}

		if (fileName == null) {
			throw new IllegalArgumentException("null fileName");
		}

		final InputStream propertyStream = loader.getResourceAsStream(fileName);
		if (propertyStream == null) {
			throw new IOException("property file unreadable " + fileName);
		}

		Properties result = new Properties();
		result.load(propertyStream);
		return result;
	}

	/**
	 * Get the {@value #APPLICATION_INCLUDES} property. That is a list of application packages or classes separated by
	 * {@link File#pathSeparator}.
	 * 
	 * @return
	 */
	public static Set<String> getApplicationIncludes() {
		String result = properties.getProperty(APPLICATION_INCLUDES);
		Assertions.notNullAssertion(result, APPLICATION_INCLUDES.concat(" not found."));
		return new HashSet<String>(Arrays.asList(result.split(File.pathSeparator)));
	}

	/**
	 * Get the {@value #MAIN_CLASS} property. That is the main class that runs the application when the program
	 * executes.
	 * 
	 * @return
	 */
	public static String getMainClass() {
		String result = properties.getProperty(MAIN_CLASS);
		Assertions.notNullAssertion(result, MAIN_CLASS.concat(" not found."));
		return result;
	}

	/**
	 * Get the {@value #INPUT_JAR_FILES} property. That is a list of the application JAR files separated by
	 * {@link File#pathSeparator}.
	 * 
	 * @return
	 */
	public static Set<String> getInputJarFiles() {
		String result = properties.getProperty(INPUT_JAR_FILES);
		Assertions.notNullAssertion(result, INPUT_JAR_FILES.concat(" not found."));
		return new HashSet<String>(Arrays.asList(result.split(File.pathSeparator)));
	}

	/**
	 * Get the {@value #LIBRARY_JAR_FILES} property. That is a list of the library JAR files separated by
	 * {@link File#pathSeparator}.
	 * 
	 * @return
	 */
	public static Set<String> getLibraryJarFiles() {
		String result = properties.getProperty(LIBRARY_JAR_FILES);
		Assertions.notNullAssertion(result, LIBRARY_JAR_FILES.concat(" not found."));
		return new HashSet<String>(Arrays.asList(result.split(File.pathSeparator)));
	}

	/**
	 * Get the names of classes that might be dynamically loaded by the input program.
	 * 
	 * @return
	 */
	public static List<String> getDynamicClasses() throws IOException {
		if (dynamicClasses == null) {
			dynamicClasses = new ArrayList<String>();
			String property = properties.getProperty(DYNAMIC_CLASSES_FILE);
			Assertions.notNullAssertion(property, DYNAMIC_CLASSES_FILE.concat(" not found."));

			BufferedReader in = new BufferedReader(new FileReader(property));
			String line;
			while ((line = in.readLine()) != null) {
				dynamicClasses.add(line);
			}
			in.close();
		}

		return dynamicClasses;
	}

	/**
	 * Get the class names of the dynamic library classes.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<String> getDynamicLibraryClasses() throws IOException {
		List<String> dynamicLibraryClasses = new ArrayList<String>();

		for (String className : getDynamicClasses()) {
			if (isLibraryClass(className)) {
				dynamicLibraryClasses.add(className);
			}
		}

		return dynamicLibraryClasses;
	}

	/**
	 * Get the class names of the dynamic application classes.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<String> getDynamicApplicationClasses() throws IOException {
		List<String> dynamicApplicationClasses = new ArrayList<String>();

		for (String className : getDynamicClasses()) {
			if (isApplicationClass(className)) {
				dynamicApplicationClasses.add(className);
			}
		}

		return dynamicApplicationClasses;
	}

	/**
	 * Get the files that contains the reflection facts produced by TamiFlex for this program.
	 * 
	 * @return
	 */
	public static String getTamiFlexFactsFile() {
		String result = properties.getProperty(TAMIFLEX_FACTS_FILE);
		Assertions.notNullAssertion(result, TAMIFLEX_FACTS_FILE.concat(" not found."));
		return result;
	}

	/**
	 * Get the {@value #OUTPUT_DIR} property. That is the directory to which Averroes will write any output
	 * files/folders.
	 * 
	 * @return
	 */
	public static String getOutputDir() {
		String result = properties.getProperty(OUTPUT_DIR);
		Assertions.notNullAssertion(result, OUTPUT_DIR.concat(" not found."));
		return result;
	}

	/**
	 * Get the {@value #JRE} property. That is the JAR file for the Java runtime environment to be used. This can be a
	 * single JAR file, or "system" to use the the current system Java runtime environment.
	 * 
	 * @return
	 */
	public static String getJre() {
		String result = properties.getProperty(JRE);
		Assertions.notNullAssertion(result, JRE.concat(" not found."));
		return result;
	}

	/**
	 * Get the JAVA_HOME environment variable.
	 * 
	 * @return
	 */
	public static String getJavaHome() {
		return System.getProperty("java.home");
	}

	/**
	 * Check if a class belongs to the application, based on the {@value #APPLICATION_INCLUDES} property.
	 * 
	 * @param probeClass
	 * @return
	 */
	public static boolean isApplicationClass(ProbeClass probeClass) {
		for (String entry : getApplicationIncludes()) {
			/*
			 * 1. If the entry ends with .* then this means it's a package. 2. If the entry ends with .** then it's a
			 * super package. 3. If the entry is **, then it's the default package. 4. Otherwise, it's the full class
			 * name.
			 */
			if (entry.endsWith(".*")) {
				String pkg = entry.replace(".*", "");
				if (probeClass.pkg().equalsIgnoreCase(pkg)) {
					return true;
				}
			} else if (entry.endsWith(".**")) {
				String pkg = entry.replace("**", "");
				if (probeClass.toString().startsWith(pkg)) {
					return true;
				}
			} else if (entry.equalsIgnoreCase("**") && probeClass.pkg().isEmpty()) {
				return true;
			} else if (entry.equalsIgnoreCase(probeClass.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a class belongs to the application, based on the {@value #APPLICATION_INCLUDES} property.
	 * 
	 * @param className
	 * @return
	 */
	public static boolean isApplicationClass(String className) {
		return isApplicationClass(ObjectManager.v().getClass(className));
	}

	/**
	 * Check if a class belongs to the application, based on the {@value #APPLICATION_INCLUDES} property.
	 * 
	 * @param sootClass
	 * @return
	 */
	public static boolean isApplicationClass(SootClass sootClass) {
		return isApplicationClass(sootClass.getName());
	}

	/**
	 * Check if a class belongs to the application, based on the {@value #APPLICATION_INCLUDES} property.
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isApplicationClass(Type type) {
		return isApplicationClass(type.toString());
	}

	/**
	 * Check if a method is an application method (i.e., contained in an application class).
	 * 
	 * @param probeMethod
	 * @return
	 */
	public static boolean isApplicationMethod(ProbeMethod probeMethod) {
		return isApplicationClass(probeMethod.cls());
	}

	/**
	 * Check if a method is an application method (i.e., contained in an application class).
	 * 
	 * @param sootMethod
	 * @return
	 */
	public static boolean isApplicationMethod(SootMethod sootMethod) {
		return isApplicationClass(sootMethod.getDeclaringClass());
	}

	/**
	 * Check if a method is an application method (i.e., contained in an application class).
	 * 
	 * @param methodSignature
	 * @return
	 */
	public static boolean isApplicationMethod(String methodSignature) {
		return isApplicationClass(Scene.v().signatureToClass(methodSignature));
	}

	/**
	 * Check if a class belongs to the library (i.e., not an application class).
	 * 
	 * @param probeClass
	 * @return
	 */
	public static boolean isLibraryClass(ProbeClass probeClass) {
		return !isApplicationClass(probeClass);
	}

	/**
	 * Check if a class belongs to the library (i.e., not an application class).
	 * 
	 * @param className
	 * @return
	 */
	public static boolean isLibraryClass(String className) {
		return !isApplicationClass(className);
	}

	/**
	 * Check if a class belongs to the library (i.e., not an application class).
	 * 
	 * @param sootClass
	 * @return
	 */
	public static boolean isLibraryClass(SootClass sootClass) {
		return !isApplicationClass(sootClass);
	}

	/**
	 * Check if a method is a library method (i.e., not an application method).
	 * 
	 * @param probeMethod
	 * @return
	 */
	public static boolean isLibraryMethod(ProbeMethod probeMethod) {
		return !isApplicationMethod(probeMethod);
	}

	/**
	 * Check if a method is a library method (i.e., not an application method).
	 * 
	 * @param sootMethod
	 * @return
	 */
	public static boolean isLibraryMethod(SootMethod sootMethod) {
		return !isApplicationMethod(sootMethod);
	}

	/**
	 * Check if a method is a library method (i.e., not an application method).
	 * 
	 * @param methodSignature
	 * @return
	 */
	public static boolean isLibraryMethod(String methodSignature) {
		return !isApplicationMethod(methodSignature);
	}
}