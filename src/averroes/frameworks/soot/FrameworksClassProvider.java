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
package averroes.frameworks.soot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import soot.ClassProvider;
import soot.ClassSource;
import soot.CoffiClassSource;
import soot.coffi.ClassFile;
import averroes.frameworks.options.FrameworksOptions;
import averroes.util.io.ClassFileResource;
import averroes.util.io.Resource;
import averroes.util.io.ZipEntryResource;

/**
 * This class provider adds Java class files from a given classpath
 * 
 * @author Karim Ali
 */
public class FrameworksClassProvider implements ClassProvider {

	private Map<String, Resource> classes;

	private static IOFileFilter classFileFilter = FileFilterUtils.suffixFileFilter("class");
	private static IOFileFilter jarFileFilter = FileFilterUtils.suffixFileFilter("jar");

	/**
	 * Construct a new class provider.
	 */
	public FrameworksClassProvider() {
		classes = new HashMap<String, Resource>();
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
	 * Prepare the classpath.
	 * 
	 */
	public void prepareClasspath() {
		FrameworksOptions.getInputs().stream().map(p -> new File(p)).forEach(p -> add(p));
		FrameworksOptions.getDependencies().stream().map(p -> new File(p)).forEach(p -> add(p));
	}

	/**
	 * Add the given file object to the classpath (it could be a class file, a
	 * folder, or a JAR file).
	 * 
	 * @param p
	 */
	public void add(File p) {
		if (classFileFilter.accept(p)) {
			addClass(p.getPath(), new ClassFileResource(p));
		} else if (jarFileFilter.accept(p)) {
			addArchive(p);
		} else if (DirectoryFileFilter.DIRECTORY.accept(p)) {
			FileUtils.listFiles(p, FileFilterUtils.or(classFileFilter, jarFileFilter), TrueFileFilter.TRUE).forEach(
					file -> add(file));
		}
	}

	/**
	 * Add a class file from a resource.
	 * 
	 * @param path
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public String addClass(String path, Resource resource) {
		System.out.println("Adding class file: " + path);

		ClassFile c = new ClassFile(path);

		try {
			InputStream stream = resource.open();
			c.loadClassFile(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String className = c.toString().replace('/', '.');

		if (classes.containsKey(className)) {
			// This means we encountered another copy of the class later on the
			// path, this should never happen!
			throw new RuntimeException("class " + className + " has already been added to this class provider.");
		} else {
			classes.put(className, resource);
		}

		return className;
	}

	/**
	 * Add an archive to the class provider.
	 * 
	 * @param file
	 * @return
	 */
	public List<String> addArchive(File file) {
		System.out.println("Adding archive: " + file.getAbsolutePath());
		List<String> result = new ArrayList<String>();

		try {
			ZipFile archive = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = archive.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".class")) {
					String className = addClass(entry.getName(), new ZipEntryResource(archive, entry));
					result.add(className);
				}
			}
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Find the class for the given className. This method is invoked by
	 * {@link soot.SourceLocator}.
	 */
	@Override
	public ClassSource find(String className) {
		if (classes.containsKey(className)) {
			Resource resource = classes.get(className);
			InputStream stream = resource.open();
			String fileName = null;
			String zipFileName = null;

			if (resource instanceof ZipEntryResource) {
				ZipEntryResource zer = (ZipEntryResource) resource;
				fileName = zer.entry().getName();
				zipFileName = zer.archive().getName();
			} else if (resource instanceof ClassFileResource) {
				ClassFileResource cfr = (ClassFileResource) resource;
				fileName = cfr.file().getPath();
			}

			return new CoffiClassSource(className, stream, fileName, zipFileName);

		} else {
			return null;
		}
	}
}