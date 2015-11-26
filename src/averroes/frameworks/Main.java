package averroes.frameworks;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import soot.ClassProvider;
import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.SourceLocator;
import soot.options.Options;
import averroes.frameworks.options.FrameworksOptions;
import averroes.frameworks.soot.CodeGenerator;
import averroes.frameworks.soot.FrameworksClassProvider;
import averroes.util.TimeUtils;

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
			FileUtils.forceMkdir(new File(FrameworksOptions.getOutputDirectory()));
			FileUtils.cleanDirectory(new File(FrameworksOptions.getOutputDirectory()));

			// Prepare the soot classpath
			TimeUtils.reset();
			System.out.println("");
			System.out.println("Preparing Averroes ...");
			FrameworksClassProvider provider = new FrameworksClassProvider();
			provider.prepareClasspath();

			// Set some soot parameters
			SourceLocator.v().setClassProviders(Collections.singletonList((ClassProvider) provider));
			// Options.v().classes().addAll(provider.getClassNames());
			Options.v().classes().add("simple.A");
			System.out.println(Options.v().soot_classpath());
			// Options.v().set_full_resolver(true);
			Options.v().set_validate(true);

			// Load the necessary classes
			TimeUtils.reset();
			System.out.println("");
			System.out.println("Loading classes ...");
			Scene.v().loadNecessaryClasses();
			double soot = TimeUtils.elapsedTime();
			System.out.println("Soot loaded the input classes in " + soot + " seconds.");

			// Now let Averroes do its thing
			TimeUtils.reset();
			System.out.println("");
			// TODO: do not call Scene.v().getClasses() here as it will throw
			// concurrent modification error when new classes
			// are added
			Scene.v().getApplicationClasses().stream().map(c -> c.getMethods()).flatMap(List::stream)
					.filter(SootMethod::isConcrete).forEach(m -> CodeGenerator.getJimpleBodyCreator(m).generateCode());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}