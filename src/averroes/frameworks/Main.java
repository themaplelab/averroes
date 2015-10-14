package averroes.frameworks;

import java.io.File;

import org.apache.commons.io.FileUtils;

import soot.G;
import soot.Scene;
import soot.options.Options;
import averroes.frameworks.options.FrameworksOptions;
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
			FrameworksOptions.processArguments(args);

			// Reset Soot
			G.reset();

			// Create the output directory and clean up any class files in there
			FileUtils.forceMkdir(new File(FrameworksOptions.getOutputDirectory()));
			FileUtils.cleanDirectory(Paths.classesOutputDirectory());

			// Set some soot parameters
			Options.v().set_full_resolver(true);
			Options.v().set_validate(true);
			
			// Load the necessary classes
			TimeUtils.reset();
			System.out.println("");
			System.out.println("Loading classes ...");
			Scene.v().loadNecessaryClasses();
			double soot = TimeUtils.elapsedTime();
			System.out.println("Soot loaded the input classes in " + soot + " seconds.");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}