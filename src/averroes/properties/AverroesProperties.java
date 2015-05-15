package averroes.properties;

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
import soot.SootClass;
import averroes.exceptions.AverroesException;
import averroes.util.io.FileUtils;

/**
 * A class that holds all the properties required by Averroes to run. For the
 * possible values of each property, you can consult the accompanying
 * properties/averroes.properties.sample file or the online tutorial at
 * {@link http ://karimali.pl/averroes}
 * 
 * @author Karim Ali
 * 
 */
public final class AverroesProperties {

	private static final String APPLICATION_INCLUDES = "application_includes";

	private static final String MAIN_CLASS = "main_class";

	private static final String INPUT_JAR_FILES = "input_jar_files";

	private static final String LIBRARY_JAR_FILES = "library_jar_files";

	private static final String ENABLE_DYNAMIC_CLASSES = "enable_dynamic_classes";

	private static final String DYNAMIC_CLASSES_FILE = "dynamic_classes_file";

	private static final String ENABLE_TAMIFLEX = "enable_tamiflex";

	private static final String TAMIFLEX_FACTS_FILE = "tamiflex_facts_file";

	private static final String OUTPUT_DIR = "output_dir";

	private static final String JRE = "jre";

	private static Properties properties = null;
	private static List<String> dynamicClasses = null;

	/**
	 * Load the properties file.
	 * 
	 * @throws AverroesException
	 */
	public static void loadProperties(String file) throws AverroesException {
		try {
			properties = loadPropertiesFromFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
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
	private static Properties loadPropertiesFromFile(String fileName) throws IOException {
		if (fileName == null) {
			throw new IllegalArgumentException("null fileName");
		}

		InputStream propertyStream = FileUtils.getResourceAsStream(fileName);

		Properties result = new Properties();
		result.load(propertyStream);
		return result;
	}

	/**
	 * Get the {@value #APPLICATION_INCLUDES} property. That is a list of
	 * application packages or classes separated by {@link File#pathSeparator}.
	 * 
	 * @return
	 */
	public static Set<String> getApplicationIncludes() {
		String result = properties.getProperty(APPLICATION_INCLUDES,
				DefaultPropertiesValues.DEFAULT_APPLICATION_INCLUDES);
		return new HashSet<String>(Arrays.asList(result.split(File.pathSeparator)));
	}

	/**
	 * Get the {@value #MAIN_CLASS} property. That is the main class that runs
	 * the application when the program executes.
	 * 
	 * @return
	 */
	public static String getMainClass() {
		return properties.getProperty(MAIN_CLASS, DefaultPropertiesValues.DEFAULT_MAIN_CLASS);
	}

	/**
	 * Get the {@value #INPUT_JAR_FILES} property. That is a list of the
	 * application JAR files separated by {@link File#pathSeparator}.
	 * 
	 * @return
	 */
	public static Set<String> getInputJarFiles() {
		String result = properties.getProperty(INPUT_JAR_FILES, DefaultPropertiesValues.DEFAULT_INPUT_JAR_FILES);
		return new HashSet<String>(Arrays.asList(result.split(File.pathSeparator)));
	}

	/**
	 * Get the {@value #LIBRARY_JAR_FILES} property. That is a list of the
	 * library JAR files separated by {@link File#pathSeparator}.
	 * 
	 * @return
	 */
	public static Set<String> getLibraryJarFiles() {
		String result = properties.getProperty(LIBRARY_JAR_FILES, DefaultPropertiesValues.DEFAULT_LIBRARY_JAR_FILES);
		return new HashSet<String>(Arrays.asList(result.split(File.pathSeparator)));
	}

	/**
	 * Get the {@value #ENABLE_DYNAMIC_CLASSES} property.
	 * 
	 * @return
	 */
	public static boolean enableDynamicClasses() {
		String result = properties.getProperty(ENABLE_DYNAMIC_CLASSES,
				DefaultPropertiesValues.DEFAULT_ENABLE_DYNAMIC_CLASSES);
		return Boolean.parseBoolean(result);
	}

	/**
	 * Get the names of classes that might be dynamically loaded by the input
	 * program.
	 * 
	 * @return
	 */
	public static List<String> getDynamicClasses() throws IOException {
		if (dynamicClasses == null) {
			dynamicClasses = new ArrayList<String>();
			String property = properties.getProperty(DYNAMIC_CLASSES_FILE,
					DefaultPropertiesValues.DEFAULT_DYNAMIC_CLASSES_FILE);

			// If a file was given, process it only if enable_dynamic is set to
			// true.
			if (enableDynamicClasses()) {
				BufferedReader in = new BufferedReader(new FileReader(property));
				String line;
				while ((line = in.readLine()) != null) {
					dynamicClasses.add(line);
				}
				in.close();
			}
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

		if (enableDynamicClasses()) {
			for (String className : getDynamicClasses()) {
				if (isLibraryClass(className)) {
					dynamicLibraryClasses.add(className);
				}
			}
		}

		return dynamicLibraryClasses;
	}

	/**
	 * Get the {@value #ENABLE_TAMIFLEX} property.
	 * 
	 * @return
	 */
	public static boolean enableTamiflex() {
		String result = properties.getProperty(ENABLE_TAMIFLEX, DefaultPropertiesValues.DEFAULT_ENABLE_TAMIFLEX);
		return Boolean.parseBoolean(result);
	}

	/**
	 * Get the files that contains the reflection facts in the TamiFlex format
	 * for this program.
	 * 
	 * @return
	 */
	public static String getTamiFlexFactsFile() {
		return properties.getProperty(TAMIFLEX_FACTS_FILE, DefaultPropertiesValues.DEFAULT_TAMIFLEX_FACTS_FILE);
	}

	/**
	 * Get the {@value #OUTPUT_DIR} property. That is the directory to which
	 * Averroes will write any output files/folders.
	 * 
	 * @return
	 */
	public static String getOutputDir() {
		return properties.getProperty(OUTPUT_DIR, DefaultPropertiesValues.DEFAULT_OUTPUT_DIR);
	}

	/**
	 * Get the {@value #JRE} property. That is the JAR file for the Java runtime
	 * environment to be used. This can be a single JAR file, or "system" to use
	 * the the current system Java runtime environment.
	 * 
	 * @return
	 */
	public static String getJre() {
		return properties.getProperty(JRE, DefaultPropertiesValues.DEFAULT_JRE);
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
	 * Check if a class belongs to the application, based on the
	 * {@value #APPLICATION_INCLUDES} property.
	 * 
	 * @param probeClass
	 * @return
	 */
	public static boolean isApplicationClass(ProbeClass probeClass) {
		for (String entry : getApplicationIncludes()) {
			/*
			 * 1. If the entry ends with .* then this means it's a package. 2.
			 * If the entry ends with .** then it's a super package. 3. If the
			 * entry is **, then it's the default package. 4. Otherwise, it's
			 * the full class name.
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
	 * Check if a class belongs to the application, based on the
	 * {@value #APPLICATION_INCLUDES} property.
	 * 
	 * @param className
	 * @return
	 */
	public static boolean isApplicationClass(String className) {
		return isApplicationClass(ObjectManager.v().getClass(className));
	}

	/**
	 * Check if a class belongs to the application, based on the
	 * {@value #APPLICATION_INCLUDES} property.
	 * 
	 * @param sootClass
	 * @return
	 */
	public static boolean isApplicationClass(SootClass sootClass) {
		return isApplicationClass(sootClass.getName());
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
}