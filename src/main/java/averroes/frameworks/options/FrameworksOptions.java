package averroes.frameworks.options;

import averroes.util.io.FileFilters;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import soot.SourceLocator;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class that holds all the properties required by Averroes to run.
 *
 * @author Karim Ali
 */
public final class FrameworksOptions {

    private static Option input =
            Option.builder("i")
                    .longOpt("input")
                    .desc("a classpath of the input files (class files, JARs, or folders)")
                    .hasArg()
                    .argName("path")
                    .required()
                    .build();

    private static Option prefix =
            Option.builder("p")
                    .longOpt("prefix")
                    .desc("a prefix for the package names of the input files")
                    .hasArg()
                    .argName("package name")
                    .required(false)
                    .build();

    private static Option deps =
            Option.builder("d")
                    .longOpt("dependencies")
                    .desc("a classpath of the dependencies (class files, JARs, or folders)")
                    .hasArg()
                    .argName("path")
                    .required(false)
                    .build();

    private static Option outputDirectory =
            Option.builder("o")
                    .longOpt("output-directory")
                    .desc("the directory to which Averroes will write any output files/folders")
                    .hasArg()
                    .argName("directory")
                    .required()
                    .build();

    private static Option jreDirectory =
            Option.builder("j")
                    .longOpt("jre")
                    .desc(
                            "the directory that contains the Java runtime environment that Averroes should model")
                    .hasArg()
                    .argName("directory")
                    .required()
                    .build();

    private static Option analysis =
            Option.builder("a")
                    .longOpt("analysis-model")
                    .desc(
                            "the analysis that Averroes should use to model the library stubs (one of rta, xta, cfa)")
                    .hasArg()
                    .argName("analysis")
                    .required()
                    .build();

    private static Option help =
            Option.builder("h")
                    .longOpt("help")
                    .desc("print out this help message")
                    .hasArg(false)
                    .required(false)
                    .build();

    private static Option enableGuards =
            Option.builder("g")
                    .longOpt("enable-guards")
                    .desc(
                            "setting this flag will make Averroes wrap a guard around any statement that is not a cast statement (or a local variable declaration)")
                    .hasArg(false)
                    .required(false)
                    .build();

    private static Option includeDependencies =
            Option.builder("w")
                    .longOpt("whole-library")
                    .desc(
                            "setting this flag will make Averroes also model all the library dependencies of the given library code (e.g., JDK)")
                    .hasArg(false)
                    .required(false)
                    .build();

    private static Options options =
            new Options()
                    .addOption(input)
                    .addOption(prefix)
                    .addOption(deps)
                    .addOption(outputDirectory)
                    .addOption(jreDirectory)
                    .addOption(analysis)
                    .addOption(help)
                    .addOption(enableGuards)
                    .addOption(includeDependencies);

    private static CommandLine cmd;

    /**
     * Process the input arguments of Averroes.
     *
     * @param args
     */
    public static void processArguments(String[] args) {
        try {
            cmd = new DefaultParser().parse(options, args);

            // Do we need to print out help messages?
            if (cmd.hasOption(help.getOpt())) {
                help();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print out some help information.
     */
    private static void help() {
        new HelpFormatter().printHelp("jar -jar averroes.jar", "", options, "", true);
        System.exit(0);
    }

    /**
     * Get a list of the input files (class files, JARs, or folders).
     *
     * @return
     */
    public static String getPrefix() {
        return cmd.getOptionValue(prefix.getOpt(), "");
    }

    /**
     * Get the package prefix, defaulting to an empty string.
     *
     * @return
     */
    public static List<String> getInputs() {
        return Arrays.asList(cmd.getOptionValue(input.getOpt()).split(File.pathSeparator));
    }

    /**
     * A list of dependencies (class files, JARs, or folders).
     *
     * @return
     */
    public static List<String> getDependencies() {
        return Arrays.asList(cmd.getOptionValue(deps.getOpt(), "").split(File.pathSeparator));
    }

    /**
     * Get the classpath used to configure Soot.
     *
     * @return
     */
    public static String getSootClassPath() {
        String deps = getDependencies().stream().collect(Collectors.joining(File.pathSeparator));
        String inputs = Paths.get("build", "classes", "java", "test").toString();
//        getInputs().stream().collect(Collectors.joining(File.pathSeparator));
        String std =
                FileUtils.listFiles(
                        new File(getJreDirectory()), FileFilters.jreFileFilter, TrueFileFilter.TRUE)
                        .stream()
                        .map(f -> f.getAbsolutePath())
                        .collect(Collectors.joining(File.pathSeparator));

        return inputs + File.pathSeparator + (deps.isEmpty() ? "" : deps + File.pathSeparator) + std;
    }

    /**
     * Get all classes under the Averroes classpath.
     *
     * @return
     */
    public static List<String> getClasses() {
        return getClasses(getPrefix() + ".");
    }

    /**
     * Get all classes under the Averroes classpath, given a package name prefix.
     * This method is useful when the input folder is not the root package folder.
     *
     * @param prefix
     * @return
     */
    public static List<String> getClasses(String prefix) {
        return getInputs().stream()
                .map(p -> SourceLocator.v().getClassesUnder(p, prefix))
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
     * The version for the Java runtime environment to be used. This can one of (1.4, 1.6, 1.7, 1.8,
     * system).
     *
     * @return
     */
    public static String getJreDirectory() {
        return cmd.getOptionValue(jreDirectory.getOpt());
    }

    /**
     * The analysis that Averroes should use to model the library stubs (one of rta, xta, cfa).
     *
     * @return
     */
    public static String getAnalysis() {
        return cmd.getOptionValue(analysis.getOpt());
    }

    /**
     * Setting this flag will make Averroes wrap a guard around any statement that is not a cast
     * statement (or a local variable declaration).
     *
     * @return
     */
    public static boolean isEnableGuards() {
        return cmd.hasOption(enableGuards.getOpt());
    }

    /**
     * Setting this flag will make Averroes also model all the library dependencies of the given
     * library code (e.g., JDK).
     *
     * @return
     */
    public static boolean isIncludeDependencies() {
        return cmd.hasOption(includeDependencies.getOpt());
    }
}
