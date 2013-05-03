package ca.uwaterloo.averroes.jar;

import java.util.Collections;

import soot.ClassProvider;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;
import soot.options.Options;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.soot.CodeGenerator;
import ca.uwaterloo.averroes.soot.Hierarchy;
import ca.uwaterloo.averroes.soot.SootSceneUtil;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * The main Averroes class.
 * 
 * @author karim
 * 
 */
public class JarFactory {

	/**
	 * The main Averroes method.
	 * 
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			// Process the arguments
			AverroesProperties.processArguments(args);
			
			// Reset Soot
			G.reset();

			// Create the output directory
			FileUtils.createDirectory(AverroesProperties.getOutputDir());

			// Organize the input JAR files
			System.out.println("");
			System.out.println("Organizing the JAR files ...");
			JarOrganizer jarOrganizer = new JarOrganizer();
			jarOrganizer.organizeInputJarFiles();

			// Print some statistics
			System.out.println("# application classes: " + jarOrganizer.applicationClassNames().size());
			System.out.println("# library classes: " + jarOrganizer.libraryClassNames().size());

			// Add the organized archives for the application and its dependencies.
			TimeUtils.reset();
			JarFactoryClassProvider provider = new JarFactoryClassProvider();
			provider.prepareJarFactoryClasspath();

			// Set some soot parameters
			SourceLocator.v().setClassProviders(Collections.singletonList((ClassProvider) provider));
			SootSceneUtil.addCommonDynamicClasses(provider);
			Options.v().classes().addAll(provider.getApplicationClassNames());
			Options.v().set_main_class(AverroesProperties.getMainClass());
			Options.v().set_validate(true);

			// Load the necessary classes
			System.out.println("");
			System.out.println("Loading classes ...");
			Scene.v().loadNecessaryClasses();
			Scene.v().setMainClassFromOptions();
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

			// Create the output directory for the library class files
			FileUtils.createDirectory(FileUtils.libraryClassesOutputDirectory());

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

			// Create empty classes for the basic classes required internally by Soot
			System.out.println("Generating empty basic library classes required by Soot ...");
			for (SootClass basicClass : Hierarchy.v().getBasicClassesDatabase().getMissingBasicClasses()) {
				CodeGenerator.writeLibraryClassFile(basicClass);
			}
			double averroes = TimeUtils.elapsedTime();
			System.out.println("Placeholder library classes created and validated in " + averroes + " seconds.");

			// Create the jar file and add all the generated class files to it.
			TimeUtils.reset();
			JarFile librJarFile = new JarFile(FileUtils.placeholderLibraryJarFile());
			librJarFile.addGeneratedLibraryClassFiles();
			librJarFile.verify();
			double bcel = TimeUtils.elapsedTime();
			System.out.println("Place holder library JAR file verified in " + bcel + " seconds.");
			System.out.println("Total time (without verification) is " + (soot + averroes) + " seconds.");
			System.out.println("Total time (with verification) is " + (soot + averroes + bcel) + " seconds.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}