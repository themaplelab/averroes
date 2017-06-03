package averroes.frameworks;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import averroes.JarFile;
import averroes.frameworks.options.FrameworksOptions;
import averroes.frameworks.soot.ClassWriter;
import averroes.frameworks.soot.CodeGenerator;
import averroes.soot.SootSceneUtil;
import averroes.util.MathUtils;
import averroes.util.TimeUtils;
import averroes.util.io.Paths;
import averroes.util.io.Printers;
import averroes.util.io.Printers.PrinterType;
import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.options.Options;

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
			FrameworksOptions.processArguments(args);

			// Reset Soot
			G.reset();

			// Create the output directory and clean up any class files in there
			FileUtils.forceMkdir(Paths.framewokrsLibraryClassesOutputDirectory());
			FileUtils.cleanDirectory(Paths.framewokrsLibraryClassesOutputDirectory());

			// Set some soot parameters
			Options.v().set_process_dir(FrameworksOptions.getInputs());
			Options.v().set_soot_classpath(FrameworksOptions.getSootClassPath());
			Options.v().set_validate(true);
			if(FrameworksOptions.isIncludeDependencies()) Options.v().set_whole_program(true);
			// Options.v().setPhaseOption("jb.tr", "use-older-type-assigner:true");

//			Options.v().setPhaseOption("wjtp", "enabled");
//			PackManager.v().getPack("wjtp").add(new StaticInlineTransform("wjtp.si"));
//			Options.v().setPhaseOption("wjtp.si", "enabled");

			// Load the necessary classes
			TimeUtils.reset();
			System.out.println("");
			System.out.println("Loading classes...");
			Scene.v().loadNecessaryClasses();
			double soot = TimeUtils.elapsedTime();
			System.out.println("Soot loaded the input classes in " + soot + " seconds.");
			
			// Add default constructors to all library classes
//			SootSceneUtil.getSortedClasses().forEach(System.out::println);
//			System.exit(0);
			SootSceneUtil.getClasses().forEach(CodeGenerator::createEmptyDefaultConstructor);

			// Now let Averroes do its thing
			TimeUtils.reset();
			System.out.println("");
			System.out.println("Creating Jimple bodies for framework methods...");
			/*
			 * Note: do not call Scene.v().getClasses() here as it will throw
			 * concurrent modification error when new classes are added.
			 */
			SootSceneUtil.getClasses().stream().map(c -> c.getMethods()).flatMap(List::stream)
					.filter(SootMethod::isConcrete).forEach(m -> CodeGenerator.getJimpleBodyCreator(m).generateCode());
			
			System.out.println("Writing JSON files for framework methods...");
			// Print out JSON files
			SootSceneUtil.getClasses().forEach(c -> {
				Printers.printJson(PrinterType.GENERATED, c);
			});
			

			// System.out.println("Optimizing generated library methods...");
			// new Optimizer().optimize();

			// System.out.println("Creating optimized Jimple bodies for library
			// methods...");
			// Scene.v().getApplicationClasses().stream().map(c ->
			// c.getMethods()).flatMap(List::stream)
			// .filter(SootMethod::isConcrete)
			// .forEach(m ->
			// Printers.printJimple(Printers.PrinterType.OPTIMIZED, m));

			// Print out JSON files
			// System.out.println("Writing JSON files for optimized library
			// methods...");
			// Scene.v().getApplicationClasses().forEach(c -> {
			// Printers.printJson(PrinterType.OPTIMIZED, c);
			// });
			
			// Perform code cleanup
//			SootUtils.cleanupClasses();

			// Write class files for the generate model
			System.out.println("Writing class files for framework methods...");
			SootSceneUtil.getClasses().forEach(ClassWriter::writeLibraryClassFile);
			double averroes = TimeUtils.elapsedTime();
			System.out.println("");
			System.out.println("Placeholder framework classes created and validated in " + averroes + " seconds.");

			// Create the jar file and add all the generated class files to it.
			TimeUtils.reset();
			JarFile frameworkJarFile = new JarFile(Paths.placeholderFrameworkJarFile());
			frameworkJarFile.addGeneratedFrameworkClassFiles();

			double bcel = TimeUtils.elapsedTime();
			System.out.println("Placeholder framework JAR file verified in " + bcel + " seconds.");
			System.out.println("");
			System.out.println("Total time (without verification) is " + MathUtils.round(soot + averroes) + " seconds.");
			System.out.println("Total time (with verification) is " + MathUtils.round(soot + averroes + bcel) + " seconds.");

			double total = TimeUtils.elapsedSplitTime();
			System.out.println("");
			System.out.println("Elapsed time: " + total + " seconds.");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}