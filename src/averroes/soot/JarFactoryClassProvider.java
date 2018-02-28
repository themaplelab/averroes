/*******************************************************************************
 * Copyright (c) 2015 Karim Ali and Ondřej Lhoták.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Karim Ali - initial API and implementation and/or initial documentation
 *******************************************************************************/
package averroes.soot;

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

import averroes.util.io.Paths;
import averroes.util.io.Resource;
import averroes.util.io.ZipEntryResource;
import soot.ClassProvider;
import soot.ClassSource;
import soot.CoffiClassSource;
import soot.coffi.ClassFile;

/**
 * This class provider adds the Java classes from the application JAR to the
 * list of application classes, and Java classes from the library JAR to the
 * list of library classes.
 * 
 * This class provider adds a class once. Any consequent additions will throw an
 * exception because each class should be encountered only once.
 * 
 * @author karim
 */
public class JarFactoryClassProvider implements ClassProvider {

	private Set<String> applicationClassNames;
	private Set<String> libraryClassNames;
	private Map<String, Resource> classes;

	/**
	 * Construct a new class provider.
	 */
	public JarFactoryClassProvider() {
		applicationClassNames = new HashSet<String>();
		libraryClassNames = new HashSet<String>();
		classes = new HashMap<String, Resource>();
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
	 * Add the organized application and library archives specified in the
	 * properties file.
	 * 
	 * @throws IOException
	 */
	public void prepareJarFactoryClasspath() throws IOException {
		System.out.println("");
		System.out.println("Preparing Averroes ...");
		addApplicationArchive();
		addLibraryArchive();
	}

	/**
	 * Add a class file in a zip/jar archive. Returns the class name of the
	 * class that was added.
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
			// This means we encountered another copy of the class later on the
			// path, this should never happen!
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
		System.out.println("Adding " + (isApplication ? "application" : "library") + " archive: "
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
		return addArchive(Paths.organizedApplicationJarFile(), true);
	}

	/**
	 * Add the organized library archive to the class provider.
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<String> addLibraryArchive() throws IOException {
		return addArchive(Paths.organizedLibraryJarFile(), false);
	}

	/**
	 * Find the class for the given className. This method is invoked by
	 * {@link soot.SourceLocator}.
	 */
	@Override
	public ClassSource find(String className) {
		if (classes.containsKey(className)) {
			Resource resource = classes.get(className);
			try {
				InputStream stream = resource.open();
				// TODO: use for soot 2.5
				// return new CoffiClassSource(className, stream);
				
				// TODO: use for soot nightly build
				ZipEntryResource zer = (ZipEntryResource) resource;
				return new CoffiClassSource(className, stream, zer.entry().getName());
			} catch (IOException exc) {
				throw new RuntimeException(exc);
			}
		} else {
			return null;
		}
	}
}