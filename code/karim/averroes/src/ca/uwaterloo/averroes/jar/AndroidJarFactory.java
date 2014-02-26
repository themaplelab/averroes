package ca.uwaterloo.averroes.jar;

import java.util.Set;

import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.soot.CodeGenerator;
import ca.uwaterloo.averroes.soot.Hierarchy;
import ca.uwaterloo.averroes.util.DexUtils;
import ca.uwaterloo.averroes.util.MathUtils;
import ca.uwaterloo.averroes.util.TimeUtils;
import ca.uwaterloo.averroes.util.io.FileUtils;

/**
 * The main Averroes class.
 * 
 * @author karim
 * 
 */
public class AndroidJarFactory {

	/**
	 * The main Averroes method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: needs to be removed when we depend on the -android flag
		Options.v().set_src_prec(Options.src_prec_apk);

		try {
			// Find the total execution time, instead of depending on the Unix time command
			TimeUtils.splitStart();

			// Process the arguments
			AverroesProperties.processArguments(args);

			// Reset Soot
			G.reset();

			// Create the output directory
			FileUtils.createDirectory(AverroesProperties.getOutputDir());

			// Get the application class names, i.e., all classes in the input android apk
			Set<String> appClasses = DexUtils.classesOfDex(AverroesProperties.getApkLocation());
			Options.v().classes().addAll(appClasses);

			// Set the soot classpath, android path, and validate jimple bodies.
			Options.v().set_soot_classpath(AverroesProperties.getAndroidAppClassPath());
			Options.v().set_src_prec(Options.src_prec_apk);
			Options.v().set_force_android_jar(AverroesProperties.defaultAndroidJar());
			Options.v().set_validate(true);

			System.out.println(AverroesProperties.getAndroidAppClassPath());

			// Load the necessary classes
			TimeUtils.reset();
			System.out.println("");
			System.out.println("Loading classes ...");
			Scene.v().loadNecessaryClasses();
			double soot = TimeUtils.elapsedTime();
			System.out.println("Soot loaded the input classes in " + soot + " seconds.");

			// Print some statistics
			System.out.println("# application classes: " + Scene.v().getApplicationClasses().size());
			System.out.println("# library classes: " + Scene.v().getLibraryClasses().size());

			// String className = "com.flurry.sdk.kg$a";
			// System.out.println(Scene.v().getSootClass(className).getFields());
			// System.exit(0);

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

			// Create the dummy main class
			System.out.println("Creating the Android dummy main class ...");
			CodeGenerator.v().createAndroidDummyMainClass();

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
			System.out.println("Placeholder library JAR file verified in " + bcel + " seconds.");
			System.out
					.println("Total time (without verification) is " + MathUtils.round(soot + averroes) + " seconds.");
			System.out.println("Total time (with verification) is " + MathUtils.round(soot + averroes + bcel)
					+ " seconds.");

			double total = TimeUtils.elapsedSplitTime();
			System.out.println("Elapsed time: " + total + " seconds.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}