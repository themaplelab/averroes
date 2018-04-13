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
package averroes;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;

import soot.ClassProvider;
import soot.DexClassProvider;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.options.Options;
import averroes.android.SetupAndroid;
import averroes.options.AverroesOptions;
import averroes.soot.CodeGenerator;
import averroes.soot.Hierarchy;
import averroes.soot.JarFactoryClassProvider;
import averroes.soot.SootSceneUtil;
import averroes.util.MathUtils;
import averroes.util.TimeUtils;
import averroes.util.io.Paths;

/**
 * The main Averroes class.
 * 
 * @author Karim Ali
 * 
 */
public class Main {

	/**
	 * The main Averroes method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Find the total execution time, instead of depending on the Unix
			// time command
			TimeUtils.splitStart();

			// Process the arguments
			AverroesOptions.processArguments(args);

			// Reset Soot
			G.reset();

			// Create the output directory and clean up any class files in there
			FileUtils.forceMkdir(Paths.libraryClassesOutputDirectory());
			FileUtils.cleanDirectory(Paths.classesOutputDirectory());

			// Organize the input JAR files
			System.out.println("");
			System.out.println("Organizing the JAR files ...");
			JarOrganizer jarOrganizer = new JarOrganizer();
			jarOrganizer.organizeInputJarFiles();

			// Print some statistics
			if (AverroesOptions.isAndroid()) {
				System.out.println(
						"# referenced application classes: " + SetupAndroid.v().getReferencedApplicationClassCount());
				System.out.println(
						"# referenced application methods: " + SetupAndroid.v().getReferencedApplicationMethodCount());
			} else {
				System.out.println("# application classes: " + jarOrganizer.applicationClassNames().size());
			}

			System.out.println("# library classes: " + jarOrganizer.libraryClassNames().size());

			// Add the organized archives for the application and its
			// dependencies.
			TimeUtils.reset();
			JarFactoryClassProvider provider = new JarFactoryClassProvider();
			provider.prepareJarFactoryClasspath();

			// Set some soot parameters for android
			if (AverroesOptions.isAndroid()) {
				List<ClassProvider> classProviders = new LinkedList<>();
				classProviders.add((ClassProvider) provider);
				classProviders.add(new DexClassProvider());
				SourceLocator.v().setClassProviders(classProviders);
				SootSceneUtil.addCommonDynamicClasses(provider);

				SetupAndroid setupAndroid = null;
				SootMethod dummyMain = null;

				setupAndroid = SetupAndroid.v();
				dummyMain = setupAndroid.getDummyMainMethod();
			}

			// Set some soot parameters if not android
			else if (!AverroesOptions.isAndroid()) {
				Options.v().classes().addAll(provider.getApplicationClassNames());
				SourceLocator.v().setClassProviders(Collections.singletonList((ClassProvider) provider));
				SootSceneUtil.addCommonDynamicClasses(provider);
				Options.v().classes().addAll(provider.getApplicationClassNames());
				Options.v().set_main_class(AverroesOptions.getMainClass());
			}
			Options.v().set_validate(true);

			// Load the necessary classes
			System.out.println("");
			System.out.println("Loading classes ...");
			Scene.v().loadNecessaryClasses();

			// only in case of java files
			if (!AverroesOptions.isAndroid()) {
				Scene.v().setMainClassFromOptions();
			}

			double soot = TimeUtils.elapsedTime();
			System.out.println("Soot loaded the input classes in " + soot + " seconds.");

			// Now let Averroes do its thing
			// First, create the class hierarchy
			TimeUtils.reset();
			System.out.println("");
			System.out.println("Creating the class hierarchy for the placeholder library ...");
			Hierarchy.v();

			// Output some initial statistics
			System.out.println("# initial application classes: " + Hierarchy.v().getApplicationClasses().size());
			System.out.println("# initial library classes: " + Hierarchy.v().getLibraryClasses().size());
			System.out.println("# initial library methods: " + Hierarchy.v().getLibraryMethodCount());
			System.out.println("# initial library fields: " + Hierarchy.v().getLibraryFieldCount());
			System.out.println("# referenced library methods: " + Hierarchy.v().getReferencedLibraryMethodCount());
			System.out.println("# referenced library fields: " + Hierarchy.v().getReferencedLibraryFieldCount());

			// Cleanup the hierarchy
			System.out.println("");
			System.out.println("Cleaning up the class hierarchy ...");
			Hierarchy.v().cleanupLibraryClasses();

			// Output some cleanup statistics
			System.out.println("# removed library methods: " + Hierarchy.v().getRemovedLibraryMethodCount());
			System.out.println("# removed library fields: " + Hierarchy.v().getRemovedLibraryFieldCount());
			// The +1 is for Finalizer.register that will be added later
			System.out.println("# final library methods: " + (Hierarchy.v().getLibraryMethodCount() + 1));
			System.out.println("# final library fields: " + Hierarchy.v().getLibraryFieldCount());

			// Output some code generation statistics
			System.out.println("");
			System.out.println("Generating extra library classes ...");
			System.out.println("# generated library classes: " + CodeGenerator.v().getGeneratedClassCount());
			System.out.println("# generated library methods: " + CodeGenerator.v().getGeneratedMethodCount());

			// Create the Averroes library class
			System.out.println("");
			System.out.println("Creating the skeleton for Averroes's main library class ...");
			CodeGenerator.v().createAverroesLibraryClass();

			// Create method bodies to the library classes
			System.out.println("Generating the method bodies for the placeholder library classes ...");
			CodeGenerator.v().createLibraryMethodBodies();

			// Create empty classes for the basic classes required internally by
			// Soot
			System.out.println("Generating empty basic library classes required by Soot ...");
			for (SootClass basicClass : Hierarchy.v().getBasicClassesDatabase().getMissingBasicClasses()) {
				CodeGenerator.writeLibraryClassFile(basicClass);
			}

			// Add all the phantom classes created by flowdroid if android
			if (AverroesOptions.isAndroid()) {
				System.out.println("Generating the phantom classes for placeholder library ...");
				Set<String> basicClasses = new HashSet<String>();
				basicClasses.addAll(Scene.v().getBasicClasses());
				for (SootClass phantomClass : CodeGenerator.v().getPhantomLibraryCLasses()) {

					// ignoring phantom classes that are basic classes
					// because they are handled above
					if (!basicClasses.contains(phantomClass.getName())) {
						CodeGenerator.writeLibraryClassFile(phantomClass);
					}
				}
			}

			double averroes = TimeUtils.elapsedTime();
			System.out.println("Placeholder library classes created and validated in " + averroes + " seconds.");

			// Create the jar file and add all the generated class files to it.
			TimeUtils.reset();
			JarFile librJarFile = new JarFile(Paths.placeholderLibraryJarFile());
			librJarFile.addGeneratedLibraryClassFiles();
			JarFile aveJarFile = new JarFile(Paths.averroesLibraryClassJarFile());
			aveJarFile.addAverroesLibraryClassFile();
			// Now verify all the generated class files
			aveJarFile.verify();
			double bcel = TimeUtils.elapsedTime();
			System.out.println("Placeholder library JAR file verified in " + bcel + " seconds.");
			System.out
					.println("Total time (without verification) is " + MathUtils.round(soot + averroes) + " seconds.");
			System.out.println(
					"Total time (with verification) is " + MathUtils.round(soot + averroes + bcel) + " seconds.");

			double total = TimeUtils.elapsedSplitTime();
			System.out.println("Elapsed time: " + total + " seconds.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void usage() {
		System.out.println("");
		System.out.println("Usage: java -jar averroes.jar [options]");
		System.out.println("  -tfx : enable Tamiflex support");
		System.out.println("  -dyn : enable dynamic classes support");
		System.out.println("");
		System.exit(1);
	}
}