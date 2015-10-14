package averroes.frameworks.options;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * A class that holds all the properties required by Averroes to run.
 * 
 * @author Karim Ali
 * 
 */
public final class FrameworksOptions {
	
	private static Option libraryJars = Option.builder("l")
			.longOpt("library-jars")
			.desc("a list of the JAR files for library dependencies separated by File.pathSeparator")
			.hasArg()
			.argName("path")
			.required()
			.build();
	
	private static Option outputDirectory = Option.builder("o")
			.longOpt("output-directory")
			.desc("the directory to which Averroes will write any output files/folders.")
			.hasArg()
			.argName("directory")
			.required()
			.build();
	
	private static Option jreDirectory = Option.builder("j")
			.longOpt("java-runtime-directory")
			.desc("the directory that contains the Java runtime environment that Averroes should model")
			.hasArg()
			.argName("directory")
			.required()
			.build();
	
	private static Option analysis = Option.builder("a")
			.longOpt("analysis")
			.desc("the analysis that Averroes should use to model the library stubs (one of rta, xta, cfa)")
			.hasArg()
			.argName("analysis")
			.required()
			.build();
	
	private static Options options = new Options()
			.addOption(libraryJars)
			.addOption(outputDirectory)
			.addOption(jreDirectory)
			.addOption(analysis);
	
	private static CommandLine cmd;

	/**
	 * Process the input arguments of Averroes.
	 * 
	 * @param args
	 */
	public static void processArguments(String[] args) {
		try {
			cmd = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The list of the library JAR files separated by {@link File#pathSeparator}
	 * 
	 * @return
	 */
	public static List<String> getLibraryJarFiles() {
		return Arrays.asList(cmd.getOptionValue(libraryJars.getOpt()).split(File.pathSeparator));
	}

	/**
	 * The directory to which Averroes will write any output files/folders.
	 * 
	 * @return
	 */
	public static String getOutputDirectory() {
		return cmd.getOptionValue(outputDirectory.getOpt());
	}

	/**
	 * The version for the Java runtime environment to be used. This can one of
	 * (1.4, 1.6, 1.7, 1.8, system).
	 * 
	 * @return
	 */
	public static String getJreDirectory() {
		return cmd.getOptionValue(jreDirectory.getOpt());
	}
}