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
	private List<String> classNames;

	private static IOFileFilter classFileFilter = FileFilterUtils.suffixFileFilter("class");
	private static IOFileFilter jarFileFilter = FileFilterUtils.suffixFileFilter("jar");
	private static IOFileFilter jreFileFilter = FileFilterUtils.or(FileFilterUtils.nameFileFilter("rt.jar"),
			FileFilterUtils.nameFileFilter("jsse.jar"), FileFilterUtils.nameFileFilter("jce.jar"),
			FileFilterUtils.nameFileFilter("charsets.jar"));

	/**
	 * Construct a new class provider.
	 */
	public FrameworksClassProvider() {
		classes = new HashMap<String, Resource>();
		classNames = new ArrayList<String>();
	}

	/**
	 * Get the set of all class names.
	 * 
	 * @return
	 */
	public List<String> getClassNames() {
		return classNames;
	}

	/**
	 * Prepare the classpath.
	 * 
	 */
	public void prepareClasspath() {
		FrameworksOptions.getInputs().stream().map(p -> new File(p)).forEach(f -> add(f, true));
		FrameworksOptions.getDependencies().stream().map(p -> new File(p)).forEach(f -> add(f, false));
		FileUtils.listFiles(new File(FrameworksOptions.getJreDirectory()), jreFileFilter, null).stream()
				.map(f -> new File(f.getAbsolutePath())).forEach(f -> add(f, false));
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

	/**
	 * Add the given file object to the classpath (it could be a class file, a
	 * folder, or a JAR file).
	 * 
	 * @param p
	 */
	private void add(File p, boolean collectClassName) {
		if (classFileFilter.accept(p)) {
			System.out.println("Adding class file: " + p);
			addClass(p.getPath(), new ClassFileResource(p), collectClassName);
		} else if (jarFileFilter.accept(p)) {
			System.out.println("Adding archive: " + p.getAbsolutePath());
			addArchive(p, collectClassName);
		} else if (DirectoryFileFilter.DIRECTORY.accept(p)) {
			FileUtils.listFiles(p, FileFilterUtils.or(classFileFilter, jarFileFilter), TrueFileFilter.TRUE).forEach(
					file -> add(file, collectClassName));
		}
	}

	/**
	 * Add a class file from a resource.
	 * 
	 * @param path
	 * @param resource
	 * @param collectClassName
	 * @throws IOException
	 */
	private void addClass(String path, Resource resource, boolean collectClassName) {
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
			if(collectClassName) {
				classNames.add(className);
			}
		}
	}

	/**
	 * Add an archive to the class provider.
	 * 
	 * @param file
	 * @param collectClassName
	 */
	private void addArchive(File file, boolean collectClassName) {
		try {
			ZipFile archive = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = archive.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".class")) {
					addClass(entry.getName(), new ZipEntryResource(archive, entry), collectClassName);
				}
			}
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}