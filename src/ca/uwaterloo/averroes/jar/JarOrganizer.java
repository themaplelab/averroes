package ca.uwaterloo.averroes.jar;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * Utility class to organize the input JAR files to Averroes into two JAR files only: one for the application, and
 * another one for the library. This process does not alter the original class files in any way, it's merely copying the
 * class files into these temporary JAR files for convenience.
 * 
 * @author karim
 * 
 */
public class JarOrganizer {

	private Set<String> classNames;
	private JarFile organizedApplicationJarFile;
	private JarFile organizedLibraryJarFile;

	private Set<String> applicationClassNames;
	private Set<String> libraryClassNames;

	/**
	 * Construct a new JAR organizer.
	 */
	public JarOrganizer() {
		classNames = new HashSet<String>();
		applicationClassNames = new HashSet<String>();
		libraryClassNames = new HashSet<String>();
		organizedApplicationJarFile = new JarFile(FileUtils.organizedApplicationJarFile());
		organizedLibraryJarFile = new JarFile(FileUtils.organizedLibraryJarFile());
	}

	/**
	 * Get the set of application class names.
	 * 
	 * @return
	 */
	public Set<String> applicationClassNames() {
		return applicationClassNames;
	}

	/**
	 * Get the set of library class names.
	 * 
	 * @return
	 */
	public Set<String> libraryClassNames() {
		return libraryClassNames;
	}

	/**
	 * Organize the input JAR files into two JAR files only: one for application classes, the other for library classes.
	 * 
	 * @throws ZipException
	 * @throws IOException
	 */
	public void organizeInputJarFiles() throws ZipException, IOException {
		processInputs();
		processDependencies();
		organizedApplicationJarFile.close();
		organizedLibraryJarFile.close();
	}

	/**
	 * Process the input JAR files.
	 * 
	 * @throws ZipException
	 * @throws IOException
	 */
	private void processInputs() throws ZipException, IOException {
		for (String entry : AverroesProperties.getInputJarFiles()) {
			if (FileUtils.isValidFile(entry)) {
				processArchive(entry, true);
			}
		}
	}

	/**
	 * Process the dependencies of the input JAR files.
	 * 
	 * @throws ZipException
	 * @throws IOException
	 */
	private void processDependencies() throws ZipException, IOException {
		for (String lib : AverroesProperties.getLibraryJarFiles()) {
			processArchive(lib, false);
		}

		// Add the JRE libraries
		if (AverroesProperties.getJre().equalsIgnoreCase("system")) {
			processArchive(AverroesProperties.getJavaHome() + File.separator + "lib" + File.separator + "rt.jar", false);
			processArchive(AverroesProperties.getJavaHome() + File.separator + "lib" + File.separator + "jce.jar",
					false);
			processArchive(AverroesProperties.getJavaHome() + File.separator + "lib" + File.separator + "jsse.jar",
					false);
		} else {
			processArchive(AverroesProperties.getJre(), false);
		}
	}

	/**
	 * Process a given JAR file.
	 * 
	 * @param fileName
	 * @param fromApplicationArchive
	 * @throws ZipException
	 * @throws IOException
	 */
	private void processArchive(String fileName, boolean fromApplicationArchive) throws ZipException, IOException {
		// Exit if the fileName is empty
		if(fileName.trim().length() <= 0) {
			return;
		}
		
		File file = new File(fileName);
		System.out.println("Processing " + (fromApplicationArchive ? "input" : "library") + " archive: "
				+ file.getAbsolutePath());

		ZipFile archive = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = archive.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().endsWith(".class")) {
				addClass(archive, entry, fromApplicationArchive);
			}
		}
		archive.close();
	}

	/**
	 * Determine whether the given class file will be added to the list of application or library class files depending
	 * on the properties file.
	 * 
	 * @param archive
	 * @param entry
	 * @param fromApplicationArchive
	 * @throws IOException
	 */
	private void addClass(ZipFile archive, ZipEntry entry, boolean fromApplicationArchive) throws IOException {
		String className = entry.getName().replace('/', '.').replace(".class", "");

		if (classNames.contains(className)) {
			/*
			 * Ignore, this means we encountered another copy of the class later on the path, it should be ignored. This
			 * is the case in some benchmarks where the jar file contains both application and library classes, while
			 * some of those library classes are also in the rt.jar or deps.jar. In such a case, we want to add the
			 * classes from the jar file first and ignore the repetition.
			 */
			// System.out.println("class " + className + " has already been added to this class provider.");
		} else {
			/*
			 * The class has to be from an application archive & an application class. Some classes in xalan are
			 * application classes based on their package name only, while they're in fact not part of the application
			 * and they come from rt.jar (e.g., org.apache.xalan.templates.OutputProperties$1).
			 */
			if (AverroesProperties.isApplicationClass(className) && fromApplicationArchive) {
				extractApplicationClassFile(archive, entry);
				applicationClassNames.add(className);
			} else {
				extractLibraryClassFile(archive, entry);
				libraryClassNames.add(className);
			}

			classNames.add(className);
		}
	}

	/**
	 * Extract an application class file.
	 * 
	 * @param sourceArchive
	 * @param entry
	 * @throws IOException
	 */
	private void extractApplicationClassFile(ZipFile sourceArchive, ZipEntry entry) throws IOException {
		extractClassFile(sourceArchive, entry, organizedApplicationJarFile);
	}

	/**
	 * Extract a library class file.
	 * 
	 * @param sourceArchive
	 * @param entry
	 * @throws IOException
	 */
	private void extractLibraryClassFile(ZipFile sourceArchive, ZipEntry entry) throws IOException {
		extractClassFile(sourceArchive, entry, organizedLibraryJarFile);
	}

	/**
	 * Extract a class file to specified file.
	 * 
	 * @param sourceArchive
	 * @param entry
	 * @param destArchive
	 * @throws IOException
	 */
	private void extractClassFile(ZipFile sourceArchive, ZipEntry entry, JarFile destArchive) throws IOException {
		// Write out the class file to the destination archive directly. No temporary file used.
		destArchive.add(sourceArchive.getInputStream(entry), entry.getName());
	}
}
