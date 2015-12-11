package averroes.frameworks.options;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import averroes.util.io.FileFilters;

/**
 * A class that holds all the properties required by Averroes to run.
 * 
 * @author Karim Ali
 * 
 */
public final class FrameworksOptions {

	private static Option input = Option
			.builder("i")
			.longOpt("input")
			.desc("a classpath of the input files (class files, JARs, or folders)")
			.hasArg().argName("path").required().build();

	private static Option deps = Option
			.builder("d")
			.longOpt("dependencies")
			.desc("a classpath of the dependencies (class files, JARs, or folders)")
			.hasArg().argName("path").required(false).build();

	private static Option outputDirectory = Option
			.builder("o")
			.longOpt("output-directory")
			.desc("the directory to which Averroes will write any output files/folders.")
			.hasArg().argName("directory").required().build();

	private static Option jreDirectory = Option
			.builder("j")
			.longOpt("jre")
			.desc("the directory that contains the Java runtime environment that Averroes should model")
			.hasArg().argName("directory").required().build();

	private static Option analysis = Option
			.builder("a")
			.longOpt("analysis-model")
			.desc("the analysis that Averroes should use to model the library stubs (one of rta, xta, cfa)")
			.hasArg().argName("analysis").required().build();

	private static Options options = new Options().addOption(input)
			.addOption(deps).addOption(outputDirectory).addOption(jreDirectory)
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
	 * A list of the input files (class files, JARs, or folders).
	 * 
	 * @return
	 */
	public static List<String> getInputs() {
		return Arrays.asList(cmd.getOptionValue(input.getOpt()).split(
				File.pathSeparator));
	}

	/**
	 * A list of dependencies (class files, JARs, or folders).
	 * 
	 * @return
	 */
	public static List<String> getDependencies() {
		return Arrays.asList(cmd.getOptionValue(deps.getOpt(), "").split(
				File.pathSeparator));
	}

	/**
	 * Get the classpath used to configure Soot.
	 * 
	 * @return
	 */
	public static String getSootClassPath() {
		String deps = getDependencies().stream().collect(
				Collectors.joining(File.pathSeparator));
		String std = FileUtils
				.listFiles(new File(getJreDirectory()),
						FileFilters.jreFileFilter, null).stream()
				.map(f -> f.getAbsolutePath())
				.collect(Collectors.joining(File.pathSeparator));

		return deps.isEmpty() ? std : deps + File.pathSeparator + std;
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

	/**
	 * The analysis that Averroes should use to model the library stubs (one of
	 * rta, xta, cfa).
	 * 
	 * @return
	 */
	public static String getAnalysis() {
		return cmd.getOptionValue(analysis.getOpt());
	}
}