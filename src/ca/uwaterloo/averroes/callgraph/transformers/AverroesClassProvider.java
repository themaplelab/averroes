package ca.uwaterloo.averroes.callgraph.transformers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import soot.ClassProvider;
import soot.ClassSource;
import soot.CoffiClassSource;
import soot.coffi.ClassFile;
import ca.uwaterloo.averroes.util.io.FileUtils;
import ca.uwaterloo.averroes.util.io.Resource;
import ca.uwaterloo.averroes.util.io.ZipEntryResource;

/**
 * This class provider adds the Java classes from the application JAR to the list of application classes, and Java
 * classes from the placeholder library JAR to the list of library classes.
 * 
 * This class provider adds a class once. Any consequent additions will throw an exception because each class should be
 * encountered only once.
 * 
 * @author karim
 */
public class AverroesClassProvider implements ClassProvider {

	private Set<String> applicationClassNames;
	private Set<String> libraryClassNames;
	private Map<String, Resource> classes;

	private String benchmark;

	/**
	 * Construct a new class provider.
	 */
	public AverroesClassProvider(String benchmark) {
		applicationClassNames = new HashSet<String>();
		libraryClassNames = new HashSet<String>();
		classes = new HashMap<String, Resource>();

		this.benchmark = benchmark;
	}

	/**
	 * Get the set of application class names.
	 * 
	 * @return
	 */
	public Set<String> getApplicationClassNames() {
		return applicationClassNames;
	}

	/**
	 * Get the set of library class names.
	 * 
	 * @return
	 */
	public Set<String> getLibraryClassNames() {
		return libraryClassNames;
	}

	/**
	 * Get the set of all class names.
	 * 
	 * @return
	 */
	public Set<String> getClassNames() {
		return classes.keySet();
	}

	/**
	 * Add the organized application and library archives specified in the properties file.
	 * 
	 * @throws IOException
	 */
	public void prepare() throws IOException {
		System.out.println("");
		System.out.println("Preparing classes ...");
		addApplicationArchive();
		addLibraryArchive();
	}

	/**
	 * Add a class file in a zip/jar archive. Returns the class name of the class that was added.
	 * 
	 * @param archive
	 * @param entry
	 * @param fromApplicationArchive
	 * @return
	 * @throws IOException
	 */
	public String addClass(ZipFile archive, ZipEntry entry, boolean fromApplicationArchive) throws IOException {
		return addClass(entry.getName(), new ZipEntryResource(archive, entry), fromApplicationArchive);
	}

	/**
	 * Add a class file from a resource.
	 * 
	 * @param path
	 * @param resource
	 * @param fromApplicationArchive
	 * @return
	 * @throws IOException
	 */
	public String addClass(String path, Resource resource, boolean fromApplicationArchive) throws IOException {
		ClassFile c = new ClassFile(path);

		InputStream stream = null;
		try {
			stream = resource.open();
			c.loadClassFile(stream);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		String className = c.toString().replace('/', '.');

		if (classes.containsKey(className)) {
			// This means we encountered another copy of the class later on the path, this should never happen!
			throw new RuntimeException("class " + className + " has already been added to this class provider.");
		} else {
			if (fromApplicationArchive) {
				applicationClassNames.add(className);
			} else {
				libraryClassNames.add(className);
			}
			classes.put(className, resource);
		}

		return className;
	}

	/**
	 * Add an archive to the class provider.
	 * 
	 * @param file
	 * @param isApplication
	 * @return
	 * @throws IOException
	 */
	public List<String> addArchive(File file, boolean isApplication) throws IOException {
		System.out.println("Adding " + (isApplication ? "application" : "placeholder library") + " archive: "
				+ file.getAbsolutePath());
		List<String> result = new ArrayList<String>();

		ZipFile archive = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = archive.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().endsWith(".class")) {
				String className = addClass(archive, entry, isApplication);
				result.add(className);
			}
		}

		return result;
	}

	/**
	 * Add an archive to the class provider.
	 * 
	 * @param file
	 * @param isApplication
	 * @return
	 * @throws IOException
	 */
	public List<String> addArchive(String file, boolean isApplication) throws IOException {
		return addArchive(new File(file), isApplication);
	}

	/**
	 * Add the organized application archive to the class provider.
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<String> addApplicationArchive() throws IOException {
		return addArchive(FileUtils.organizedApplicationJarFile(benchmark), true);
	}

	/**
	 * Add the organized library archive to the class provider.
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<String> addLibraryArchive() throws IOException {
		return addArchive(FileUtils.placeholderLibraryJarFile(benchmark), false);
	}

	/**
	 * Find the class for the given className. This method is invoked by {@link soot.SourceLocator}.
	 */
	@Override
	public ClassSource find(String className) {
		if (classes.containsKey(className)) {
			Resource resource = classes.get(className);
			try {
				InputStream stream = resource.open();
				return new CoffiClassSource(className, stream); // TODO: use this for soot 2.5
				// return new CoffiClassSource(className, stream, "", ""); // TODO: fix for nightly version
			} catch (IOException exc) {
				throw new RuntimeException(exc);
			}
		} else {
			return null;
		}
	}
}