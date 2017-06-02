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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import averroes.frameworks.options.FrameworksOptions;
import soot.ClassProvider;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

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
	public static Set<SootClass> getClasses() {
		Set<SootClass> result = new HashSet<SootClass>(Scene.v().getApplicationClasses());
		if (FrameworksOptions.isIncludeDependencies()) result.addAll(Scene.v().getLibraryClasses());
		return result;

	}

	/**
	 * Check if the method is native, then change it to be not native. This way
	 * Averroes will create a method body for it. This is important because if
	 * such method has a RefLikeType return variable, we need its objects to be
	 * propagated to the LPT.
	 * 
	 * @param libraryMethod
	 */
	public static void makeNotNative(SootMethod libraryMethod) {
		if (libraryMethod.isNative()) {
			// This is a stupid workaround because Soot doesn't allow changing
			// the modifiers of a library method
			libraryMethod.getDeclaringClass().setApplicationClass();
			libraryMethod.setModifiers(libraryMethod.getModifiers() & ~Modifier.NATIVE);
			libraryMethod.getDeclaringClass().setLibraryClass();
		}
	}

	/**
	 * Check if the method is not public, make it public. Useful for Averroes to
	 * make public constructors for library classes.
	 * 
	 * @param libraryMethod
	 */
	public static void makePublic(SootMethod libraryMethod) {
		if (!libraryMethod.isPublic()) {
			// This is a stupid workaround because Soot doesn't allow changing
			// the modifiers of a library method
			libraryMethod.getDeclaringClass().setApplicationClass();
			libraryMethod.setModifiers(Modifier.PUBLIC);
			libraryMethod.getDeclaringClass().setLibraryClass();
		}
	}

	/**
	 * Get all the static fields in a given class.
	 * 
	 * @param cls
	 * @return
	 */
	public static Set<SootField> getStaticFields(SootClass cls) {
		Set<SootField> result = new HashSet<SootField>();
	
		for (SootField field : cls.getFields()) {
			if (field.isStatic()) {
				result.add(field);
			}
		}
	
		return result;
	}

	/**
	 * Get all the instance fields in a given class.
	 * 
	 * @param cls
	 * @return
	 */
	public static Set<SootField> getInstanceFields(SootClass cls) {
		Set<SootField> result = new HashSet<SootField>();
	
		for (SootField field : cls.getFields()) {
			if (!field.isStatic()) {
				result.add(field);
			}
		}
	
		return result;
	}
	
	/**
	 * Add a default constructor to the given library class.
	 * 
	 * @param libraryClass
	 */
	public static void addDefaultConstructor(SootClass cls) {
		if (!cls.isInterface()) {
			if (hasDefaultConstructor(cls)) {
				makePublic(cls.getMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG));
			} else {
				addMethodToLibraryClass(cls, getNewDefaultConstructor());
			}
		}
	}

	/**
	 * Check if the given class has a default constructor.
	 * 
	 * @param cls
	 * @return
	 */
	public static boolean hasDefaultConstructor(SootClass cls) {
		return cls.declaresMethod(Names.DEFAULT_CONSTRUCTOR_SUBSIG);
	}

	/**
	 * Get a new default constructor method.
	 * 
	 * @return
	 */
	public static SootMethod getNewDefaultConstructor() {
		return new SootMethod(SootMethod.constructorName, Collections.<Type> emptyList(), VoidType.v(), Modifier.PUBLIC);
	}
	
	/**
	 * Add a method to a library class and update the library method count.
	 * 
	 * @param libraryClass
	 * @param method
	 */
	public static void addMethodToLibraryClass(SootClass cls, SootMethod method) {
		cls.addMethod(method);
	}
}