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

import java.util.Collection;

import averroes.frameworks.options.FrameworksOptions;
import soot.ClassProvider;
import soot.Scene;
import soot.SootClass;

/**
 * A utility class for the {@link Scene} class.
 * 
 * @author karim
 * 
 */
public class SootSceneUtil {

	/**
	 * Add a basic class to the {@link Scene}.
	 * 
	 * @param provider
	 * @param className
	 */
	private static void addCommonDynamicClass(ClassProvider provider, String className) {
		if (provider.find(className) != null) {
			Scene.v().addBasicClass(className);
		}
	}

	/**
	 * Add the common dynamic classes to the {@link Scene} as basic classes.
	 * 
	 * @param provider
	 */
	public static void addCommonDynamicClasses(ClassProvider provider) {
		/*
		 * For simulating the FileSystem class, we need the implementation of
		 * the FileSystem, but the classes are not loaded automatically due to
		 * the indirection via native code.
		 */
		addCommonDynamicClass(provider, "java.io.UnixFileSystem");
		addCommonDynamicClass(provider, "java.io.WinNTFileSystem");
		addCommonDynamicClass(provider, "java.io.Win32FileSystem");

		/* java.net.URL loads handlers dynamically */
		addCommonDynamicClass(provider, "sun.net.www.protocol.file.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.ftp.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.http.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.https.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.jar.Handler");
	}

	/**
	 * Return the set of classes that Averroes processes in the Scene. These
	 * include the library classes if
	 * {@link FrameworksOptions#isIncludeDependencies()} return true.
	 * 
	 * @return
	 */
	public static Collection<SootClass> getClasses() {
		Collection<SootClass> result = Scene.v().getApplicationClasses();
		if (FrameworksOptions.isIncludeDependencies()) result.addAll(Scene.v().getLibraryClasses());
		return result;

	}
}